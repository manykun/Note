# 用户
from Note import Note

class User:
    def __init__(self, id, username, password, email, uid):
        self.id = id
        self.username = username
        self.password = password
        self.email = email
        self.uid = uid
        self.notes = []

    def __str__(self):
        return str(self.__dict__)
