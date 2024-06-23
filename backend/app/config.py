class BaseConfig:
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    MAIL_SERVER = 'smtp.163.com'
    MAIL_PORT = 465
    MAIL_USE_SSL = True
    MAIL_USERNAME = 'manykun@163.com'
    MAIL_PASSWORD = 'EDKLLVBZQYAJUKIM'
    MAIL_DEFAULT_SENDER = 'manykun@163.com'

class DevelopmentConfig(BaseConfig):
    DEBUG = True
    SQLALCHEMY_DATABASE_URI = 'sqlite:///dev_site.db'

class ProductionConfig(BaseConfig):
    DEBUG = False
    SQLALCHEMY_DATABASE_URI = 'sqlite:///prod_site.db'