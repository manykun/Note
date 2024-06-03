# 笔记数据结构

class Note:

    __user_email = ''
    __id = 0
    __title = ''
    __content = ''
    __creation = ''
    __updated_at = ''
    __attentionList = []

    def __init__(self, id, title, content, creation, updated_at):
        self.__id = id
        self.__title = title
        self.__content = content
        self.__creation = creation
        self.__updated_at = updated_at

    def __str__(self):
        return str(self.__dict__)
    
    def get_id(self):
        return self.__id
    
    def get_title(self):
        return self.__title
    
    def get_content(self):
        return self.__content
    
    def get_creation(self):
        return self.__creation
    
    def get_updated_at(self):
        return self.__updated_at
    
    def get_attentionList(self):
        return self.__attentionList
    
    def set_id(self, id):
        self.__id = id

    def set_title(self, title):
        self.__title = title

    def set_content(self, content):
        self.__content = content

    def set_creation(self, creation):
        self.__creation = creation

    def set_updated_at(self, updated_at):
        self.__updated_at = updated_at

    def set_attentionList(self, attentionList):
        self.__attentionList = attentionList

    def set_user_email(self, user_email):
        self.__user_email = user_email

    def get_user_email(self):
        return self.__user_email
    