import os
from flask import Flask
from flask_mail import Mail, Message


mail = Mail()

def create_app():
    app = Flask(__name__)

    app.config['MAIL_SERVER'] = 'smtp.163.com'
    app.config['MAIL_PORT'] = 465
    app.config['MAIL_USE_SSL'] = True
    app.config['MAIL_USERNAME'] = 'manykun@163.com'
    app.config['MAIL_PASSWORD'] = 'EDKLLVBZQYAJUKIM'
    app.config['MAIL_DEFAULT_SENDER'] = 'manykun@163.com'

    mail.init_app(app)

    from . import register
    app.register_blueprint(register.reg)

    from . import note
    app.register_blueprint(note.note)
    
    return app