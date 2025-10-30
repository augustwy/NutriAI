# lancedb_server.py
import importlib
import sys
import subprocess

# --- 自动依赖安装逻辑 ---
def install_and_import(package_name, import_name=None):
    """
    检查并安装 Python 包，然后导入它。
    :param package_name: pip 中的包名，如 "lancedb"
    :param import_name: Python 代码中 import 的名字，如 "lancedb"。如果为 None，则与 package_name 相同。
    """
    if import_name is None:
        import_name = package_name

    try:
        importlib.import_module(import_name)
        print(f"✅ Package '{import_name}' is already installed.")
    except ImportError:
        print(f"❌ Package '{import_name}' not found. Attempting to install '{package_name}'...")
        try:
            # 在 Windows 上，subprocess.check_call 需要一个列表
            # 在 Linux/macOS 上，字符串也可以，但列表更通用
            subprocess.check_call([sys.executable, "-m", "pip", "install", package_name])
            print(f"✅ Successfully installed '{package_name}'.")
            # 安装后再次尝试导入
            importlib.import_module(import_name)
        except subprocess.CalledProcessError as e:
            print(f"🚨 Failed to install '{package_name}'. Error: {e}")
            print("Please install it manually using: pip install " + package_name)
            sys.exit(1) # 退出程序，因为无法继续运行
        except Exception as e:
            print(f"🚨 An unexpected error occurred during installation/import of '{package_name}': {e}")
            sys.exit(1)

# 在启动时检查并安装所有依赖
# 注意：fastapi[all] 是一个特殊的安装方式，需要单独处理
print("Checking and installing dependencies...")
install_and_import("fastapi", "fastapi")
# "uvicorn[standard]" 包含了额外的依赖以提高性能
install_and_import("uvicorn[standard]", "uvicorn")
install_and_import("lancedb", "lancedb")
install_and_import("pydantic", "pydantic")
print("All dependencies are ready.\n")


# --- 导入依赖（此时已确保存在） ---
import lancedb
import os
import uvicorn
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional, Dict, Any, List
import argparse

# --- 数据模型定义 ---
class Document(BaseModel):
    id: str
    content: str
    metadata: Optional[Dict[str, Any]] = None
    vector: List[float]

class SearchRequest(BaseModel):
    query_vector: List[float]
    limit: int = 5

class SearchResult(BaseModel):
    id: str
    content: str
    metadata: Optional[Dict[str, Any]] = None
    score: float

class DeleteRequest(BaseModel):
    ids: List[str]

# --- FastAPI 应用初始化 ---
app = FastAPI(
    title="LanceDB Proxy Service (Auto-Install)",
    description="A Python service that auto-installs its dependencies."
)

# --- LanceDB 连接管理 ---
db_uri = os.environ.get("LANCEDB_URI", "./data/lancedb")
db = None

def get_db():
    global db
    if db is None:
        print(f"Connecting to LanceDB at: {db_uri}")
        db = lancedb.connect(db_uri)
    return db

# --- API 端点 ---
@app.on_event("startup")
async def startup_event():
    get_db()
    print("LanceDB Proxy Service is ready.")

@app.get("/health")
async def health_check():
    return {"status": "healthy"}

@app.post("/tables/{table_name}")
async def create_table(table_name: str):
    try:
        get_db() # 检查连接
        return {"message": f"Table '{table_name}' is ready for operations."}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/tables/{table_name}/add")
async def add_documents(table_name: str, documents: List[Document]):
    try:
        db = get_db()
        data = [doc.dict() for doc in documents]

        if table_name not in db.table_names():
            print(f"Table '{table_name}' not found, creating it.")
            db.create_table(table_name, data=data)
        else:
            table = db.open_table(table_name)
            table.add(data)

        return {"message": f"Added {len(documents)} documents to table '{table_name}'."}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/tables/{table_name}/search", response_model=List[SearchResult])
async def search_table(table_name: str, request: SearchRequest):
    try:
        db = get_db()
        if table_name not in db.table_names():
            raise HTTPException(status_code=404, detail=f"Table '{table_name}' not found.")

        table = db.open_table(table_name)
        results = table.search(request.query_vector).limit(request.limit).to_df()

        search_results = []
        for _, row in results.iterrows():
            search_results.append(SearchResult(
                id=row['id'],
                content=row['content'],
                metadata=row.get('metadata'),
                score=row.get('_distance', 0.0)
            ))
        return search_results
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/tables/{table_name}/delete")
async def delete_documents(table_name: str, request: DeleteRequest):
    try:
        db = get_db()
        if table_name not in db.table_names():
            raise HTTPException(status_code=404, detail=f"Table '{table_name}' not found.")

        table = db.open_table(table_name)
        # LanceDB 使用 delete 方法根据条件删除文档
        # 我们将构建一个条件表达式来删除指定 ID 的文档
        condition = "id IN (" + ",".join([f"'{id}'" for id in request.ids]) + ")"
        table.delete(condition)
        
        return {"message": f"Deleted {len(request.ids)} documents from table '{table_name}'."}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# --- 主程序入口 ---
if __name__ == "__main__":
    # 使用 argparse 解析命令行参数
    parser = argparse.ArgumentParser(description="LanceDB Proxy Service")
    parser.add_argument(
        "--host",
        type=str,
        default="127.0.0.1",
        help="Host to bind the server to"
    )
    parser.add_argument(
        "--port",
        type=int,
        default=8001,
        help="Port to bind the server to"
    )
    args = parser.parse_args()

    print(f"Starting LanceDB server on {args.host}:{args.port}")

    # 将解析出的参数传递给 uvicorn
    uvicorn.run(app, host=args.host, port=args.port)
