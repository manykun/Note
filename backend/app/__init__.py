from flask import Flask
from flask_mail import Mail
from flask_sqlalchemy import SQLAlchemy
from flask_migrate import Migrate
from app.config import DevelopmentConfig
import os

mail = Mail()
db = SQLAlchemy()
migrate = Migrate()

def create_app():
    app = Flask(__name__)
    app.config.from_object(DevelopmentConfig)

    db.init_app(app)
    migrate.init_app(app, db)
    mail.init_app(app)

    from .blueprints import register
    app.register_blueprint(register.reg)

    from .blueprints import note
    app.register_blueprint(note.note)

    from .blueprints import user
    app.register_blueprint(user.user)
    
    return app