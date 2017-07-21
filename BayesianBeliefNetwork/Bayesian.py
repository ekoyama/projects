class Node:
    def __init__(self, id, name=''):
        self.id = id
        self.name = name
        self.values = []
        self.parents = []
        self.children = []
        self.conditionalTable = {}
    
    def __eq__(self, other):
        if isinstance(other, self.__class__):
            return self.id == other.id       
        return False
     
    def __str__(self):
        return self.name
        
    def __repr__(self):
       return self.name
    
    def __hash__(self):
        return hash(self.id)
        
    def addParent(self, parent):
        if parent not in self.parents:
            self.parents.append(parent)
    
    def addChild(self, child):
        if child not in self.children:
            self.children.append(child)
        
   