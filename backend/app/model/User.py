# 用户

class User:
    def __init__(self, id, username, password, email, phone, role, status, create_time, update_time):
        self.id = id
        self.username = username
        self.password = password
        self.email = email
        self.phone = phone
        self.role = role
        self.status = status
        self.create_time = create_time
        self.update_time = update_time

    def __str__(self):
        return str(self.__dict__)
