# milvus_lite_server.py
import argparse
import sys
import subprocess
import importlib
import os
import time

# --- 自动依赖安装逻辑 ---
def install_and_import(package_name, import_name=None):
    """
    检查并安装 Python 包，然后导入它。
    """
    if import_name is None:
        import_name = package_name

    try:
        importlib.import_module(import_name)
        print(f"✅ Package '{import_name}' is already installed.")
    except ImportError:
        print(f"❌ Package '{import_name}' not found. Attempting to install '{package_name}'...")
        try:
            subprocess.check_call([sys.executable, "-m", "pip", "install", package_name])
            print(f"✅ Successfully installed '{package_name}'.")
            importlib.import_module(import_name)
        except subprocess.CalledProcessError as e:
            print(f"🚨 Failed to install '{package_name}'. Error: {e}")
            print("Please install it manually using: pip install " + package_name)
            sys.exit(1)
        except Exception as e:
            print(f"🚨 An unexpected error occurred during installation/import of '{package_name}': {e}")
            sys.exit(1)

# 在启动时检查并安装所有依赖
print("Checking and installing dependencies for Milvus Lite...")
install_and_import("milvus", "milvus")
print("All dependencies are ready.\n")


# --- 导入依赖（此时已确保存在） ---
from milvus import default_server

# --- 主程序入口 ---
if __name__ == "__main__":
    # 使用 argparse 解析命令行参数
    parser = argparse.ArgumentParser(description="Milvus Lite Embedded Server")
    parser.add_argument(
        "--port",
        type=int,
        default=19530,
        help="Port for Milvus Lite to listen on (default: 19530)."
    )
    parser.add_argument(
        "--data-dir",
        type=str,
        default="./milvus_data",
        help="Directory to store Milvus Lite data (default: ./milvus_data)."
    )
    args = parser.parse_args()

    # --- 配置并启动 Milvus Lite ---
    try:
        # 设置数据存储目录
        default_server.set_base_dir(args.data_dir)
        print(f"Milvus Lite data directory set to: {os.path.abspath(args.data_dir)}")

        # 启动服务器并指定端口 (新API)
        default_server.config.set("proxy_port", args.port)
        default_server.start()
        print(f"Attempting to start Milvus Lite on port: {args.port}")

        print("\n" + "="*50)
        print(f"🚀 Milvus Lite started successfully!")
        print(f"   - URI: http://localhost:{args.port}")
        print(f"   - Data Dir: {os.path.abspath(args.data_dir)}")
        print("="*50 + "\n")

        # 保持脚本运行，直到被手动终止 (Ctrl+C)
        while True:
            time.sleep(1)

    except Exception as e:
        print(f"\n🚨 Failed to start Milvus Lite. Error: {e}")
        import traceback
        traceback.print_exc()
        print("SERVER_START_FAILED")
        sys.exit(1)

    except KeyboardInterrupt:
        print("\n🛑 Shutdown signal received. Stopping Milvus Lite...")
        default_server.stop()
        print("Milvus Lite stopped.")
