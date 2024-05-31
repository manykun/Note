from flask import Flask
from flask_mail import Mail, Message
from app import create_app

app = create_app()

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8888)