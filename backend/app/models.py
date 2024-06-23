from app import db

class User(db.Model):
    __tablename__ = 'user'
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    uid = db.Column(db.String(6), unique=True)
    email = db.Column(db.String(50), unique=True)
    password = db.Column(db.String(50))
    code = db.Column(db.String(6))
    code_created_time = db.Column(db.Integer)
    avatar = db.Column(db.String(50))
    username = db.Column(db.String(50))
    signature = db.Column(db.String(50))
    
class Note(db.Model):
    __tablename__ = 'note'
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    uid = db.Column(db.String(6))
    title = db.Column(db.String(50))
    content = db.Column(db.Text)
    tags = db.Column(db.String(50))
    audio = db.Column(db.Text)
    images = db.Column(db.Text)

    