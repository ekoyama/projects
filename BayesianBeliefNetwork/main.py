import csv
import math
import random
import operator
import pickle
import itertools
import copy
from Bayesian import Node

def getData():
    variableNames = []
    variableValues = []
    with open('dataset/adult.variables', 'r') as f:
        for line in f:
            variable = line.split(":")
            variableNames.append(variable[0])
            variableValues.append(variable[1].strip().split(", "))
    
    data = []
    numVars = len(variableNames)
    for i in range(numVars):
        data.append([])
    
    with open('dataset/adult.data', 'r') as f:
        reader = csv.reader(f)
        for row in reader:
            for i in range(numVars):
                data[i].append(row[i].strip())                    
    return (variableNames, variableValues, data)
    
def storeData(data, filename):
    with open(filename, 'wb') as f:
        pickle.dump(data, f)
    
def retrieveData(filename):
    with open(filename, 'rb') as f:
        data = pickle.load(f)
    return data

###Transform continuous variable by percentile
def getTransCont(variableValues, data, maxbins=3): 
    contIndex = []
    for i in range(len(variableValues)):
        if len(variableValues[i]) == 1:
            contIndex.append(i)
    
    unknown = {}
    transCont = {}
    for index in contIndex:
        unknown[index] = []
        sortData = []
        for i in range(len(data[index])):
            try:
                sortData.append(int(data[index][i]))
                data[index][i] = int(data[index][i])
            except:
                unknown[index].append(i)
        
        sortData.sort()
        bins = [sortData[0]]
        currBin = 1
        nextBin = len(sortData)//maxbins
        for i in range(len(sortData)):
            if i > nextBin:
                currBin = currBin + 1
                bins.append(sortData[i])
                nextBin = currBin * len(sortData)//maxbins
        bins.append(sortData[len(sortData)-1])
        transCont[index] = bins
    return (transCont, unknown)
    
    
def translateContinuous(variableValues, data, unknown={}, transform={}):
    for index in transform:
        bins = transform[index]
        variableValues[index] = []
        min = bins[0]
        max = bins[len(bins)-1]
        if min == max:
            variablesValues[index].append((min, min, "="))
            variablesValues[index].append((min, min, "!="))
            
        else:
            pairs = []
            if min == bins[1]:
                variableValues[index].append((min, min, "<="))
            else:
                variableValues[index].append((min, min, "<"))
                
            pairs.append((min, min)) 
            prev = min
            for bin in bins[1:len(bins)-1]:
                if (prev, bin) not in pairs:
                    if prev == bin:
                        variableValues[index].append((prev, bin, "="))
                    else:
                        variableValues[index].append((prev, bin))
                    pairs.append((prev, bin))
                    prev = bin       

            if max == bins[len(bins)-2]:
                variableValues[index].append((bins[len(bins)-2], bins[len(bins)-2], ">=")) 
            else:
                variableValues[index].append((bins[len(bins)-2], bins[len(bins)-2], ">"))
        
        unknownList = unknown[index]
        for i in range(len(data[index])):
            if i not in unknownList:
                for j in variableValues[index]:
                    if isinstance(data[index][i], tuple):
                        break
                        
                    if len(j) == 2:
                        if j[0] <= data[index][i] and data[index][i] < j[1]:
                            data[index][i] = j
                    else:
                        if j[2] == "!=" and j[0] != data[index][i]:
                            data[index][i] = j
                                
                        elif j[2] == "=" and j[0] == data[index][i]:
                            data[index][i] = j
                        
                        elif j[2] == "<" and data[index][i] < j[1]:
                            data[index][i] = j
                        
                        elif j[2] == "<=" and data[index][i] <= j[1]:
                            data[index][i] = j
                        
                        elif j[2] == ">" and data[index][i] > j[0]:
                            data[index][i] = j
   
                        elif j[2] == ">=" and data[index][i] >= j[0]:
                            data[index][i] = j
   
###Get count of each value in the categorical dataset   
def getCategorialDataStat(variableValues, data):
    numVars = len(variableValues)
    for i in range(numVars):
        if(len(variableValues[i]) > 1):
            testData = data[i]
            nums = [0]
            values = len(variableValues[i])
            for j in range(values):
                nums.append(0)
            
            for j in range(len(testData)):
                try:
                    index = variableValues[i].index(testData[j])
                    nums[index] = nums[index] + 1
                except:
                    nums[values] = nums[values]
                
            for j in range(values):
                print(variableValues[i][j], ": ", nums[j])                
            print("N/A: ", nums[values])
            print("\n")
    

###Manual transformation of education and native-country
def defineTransCat(variableNames, variableValues):
    transCat = {}
    index = variableNames.index("native-country")
    transVar = variableValues[index]
    transMatrix = []
    for i in range(len(transVar)):
        if transVar[i] == "United-States":
            transMatrix.append((transVar[i], "United-States"))
        else:
            transMatrix.append((transVar[i], "Foreign"))
    transCat[index] = transMatrix 
    
    index = variableNames.index("education")
    transVar = variableValues[index]
    transMatrix = []
    No_HS = ["1st-4th", "10th", "9th", "7th-8th", "12th", "11th", "5th-6th", "Preschool"]
    for i in range(len(transVar)):
        if transVar[i] in No_HS:
            transMatrix.append((transVar[i], "No_HS"))
        else:
            transMatrix.append((transVar[i], transVar[i]))
            
    transCat[index] = transMatrix 
    return transCat

###Translate Categorical data    
def translateCategorical(variableValues, data, transform={}):
    for index in transform:
        transformMatrix = transform[index]
        variableValues[index] = []
        for i in range(len(transformMatrix)):
            if transformMatrix[i][1] not in variableValues[index]:
                variableValues[index].append(transformMatrix[i][1])
                
        for i in range(len(data[index])):
            for j in range(len(transformMatrix)):
                if data[index][i] == transformMatrix[j][0]:            
                    data[index][i] = transformMatrix[j][1]
                    break

def mutualInformation(variableValues, data, unknown):
    MI = {}
    for i in range(len(variableValues)):
        for j in range(i+1, len(variableValues)):
            datasetI = data[i]
            datasetJ = data[j]
            
            totalCount = 0
            totalI = {}
            totalJ = {}
            pairs = {}
            for k in range(len(datasetI)):
                if (i not in unknown or k not in unknown[i]) and (j not in unknown or k not in unknown[j]):
                    if datasetI[k] not in totalI:
                        totalI[datasetI[k]] = 1
                    else:
                        totalI[datasetI[k]] = totalI[datasetI[k]] + 1
                    if datasetJ[k] not in totalJ:
                        totalJ[datasetJ[k]] = 1
                    else:
                        totalJ[datasetJ[k]] = totalJ[datasetJ[k]] + 1
                    if(datasetI[k], datasetJ[k]) not in pairs:
                        pairs[(datasetI[k], datasetJ[k])] = 1
                    else:
                        pairs[(datasetI[k], datasetJ[k])] = pairs[(datasetI[k], datasetJ[k])] + 1                   
                    totalCount = totalCount + 1
            
            info = 0
            for pair in pairs:
                pxy = pairs[pair]/totalCount
                px = totalI[pair[0]]/totalCount
                py = totalJ[pair[1]]/totalCount
                info = info + pxy * math.log(pxy/(px*py)) 
            MI[(i, j)] = info
    return MI

def findPath(edge, Edges, direct=False):
    paths = [[edge[0]]]
    find = edge[1]
    while len(paths) > 0:
        currPath = paths.pop(0)
        if find in currPath:
            return currPath
        for e in Edges:
            if e[0] == currPath[-1] and e[1] not in currPath:
                currPath.append(e[1])
                paths.append(currPath[:])
                currPath.pop()
            if not direct and e[1] == currPath[-1] and e[0] not in currPath:
                currPath.append(e[0])
                paths.append(currPath[:])
                currPath.pop()
    return []
    
def findBlockSet(edge, Edges, ordering):
    blockset = []
    blocksetpaths = []
    blocksetChangePoints = []
    paths = [[edge[0]]]
    changePoints = [[]]
    find = edge[1]
    while len(paths) > 0:
        currPath = paths.pop(0)
        changePoint = changePoints.pop(0) 
        if find in currPath:
            if len(currPath) == 3:
                blockset.append(currPath[1])
            else:
                blocksetpaths.append(currPath[1:len(currPath)-1])
                blocksetChangePoints.append(changePoint[:])
                
        elif len(set(blockset).intersection(currPath)) == 0:
            for e in Edges:
                if e[0] == currPath[-1] and e[1] not in currPath:
                    currPath.append(e[1])
                    paths.append(currPath[:])
                    currPath.pop()
                    changePoint.append("F")
                    changePoints.append(changePoint[:])
                    changePoint.pop()
                    
                if e[1] == currPath[-1] and e[0] not in currPath:
                    currPath.append(e[0])
                    paths.append(currPath[:])
                    currPath.pop()
                    changePoint.append("B")
                    changePoints.append(changePoint[:])
                    changePoint.pop()
                    
    for i in range(len(blocksetpaths)):                
        block = blocksetpaths[i]
        change = blocksetChangePoints[i]
        if "B" not in change:
            add = True
            nodeblock = block[0]
            for node in block:
                if ordering.index(nodeblock.id) < ordering.index(node.id):
                    nodeblock = node
                if node in blockset:
                    add = False
            if add:
                blockset.append(nodeblock)
        else:
            if change[0] == "F":
                i = change.index("B")
                if block[i-1] not in blockset:
                    blockset.append(block[i-1])
               
            else:
                i = change.index("F")
                if block[i-1] not in blockset:
                    blockset.append(block[i-1])
                
    return blockset
    
    
def condIndependence(edge, Edges, blockset, order):
    path = findPath(edge, Edges)
    if len(path) == 0:
        return True
        
    Nodes = [edge[0], edge[1]]
    for node in blockset:
        Nodes.append(node)
    
    aEdges = []
    needsUpdate = True
    checkNodes = Nodes[:]
    while len(checkNodes) > 0:
        tempNodes = []
        for e in Edges:
            if e[1] in checkNodes and e not in aEdges: 
                aEdges.append(e)
                tempNodes.append(e[0])
        checkNodes = tempNodes[:]
    
    tempEdges = aEdges[:]
    tempEdges.append(edge)
    for i in range(len(tempEdges)):
        for j in range(i+1, len(tempEdges)):
            e1 = tempEdges[i]
            e2 = tempEdges[j]
            if e1[1] == e2[1] and (e1[0], e2[0]) not in aEdges and (e2[0], e1[0]) not in aEdges:
                candidateEdge = (e1[0], e2[0])
                if order.index(e1[0].id) > order.index(e2[0].id): 
                    candidateEdge = (e2[0], e1[0])
                if len(findPath(candidateEdge, aEdges, direct=True)) == 0: 
                    aEdges.append(candidateEdge)
        
    for node in blockset:
        tempEdges = aEdges[:]
        for e in tempEdges:
            if node == e[0] or node == e[1]:
                aEdges.remove(e)            
    path = findPath(edge, aEdges)
    if len(path) == 0:
        return True
    return False
        
    
def buildTree(variableNames, variableValues, MI, threshold=0.01, rand=False, force=[], ordering=[]):
    Nodes = []
    Edges = []
    
    Ordering = [i for i in range(len(variableNames))]
    if rand:
        random.seed(3)
        random.shuffle(Ordering)
    if len(ordering) > 0:
        Ordering = ordering[:]

        
    for i in range(len(variableNames)):
        n = Node(i, variableNames[i])
        n.values = variableValues[i][:]
        Nodes.append(n)
    
    #set MI
    S = []
    for infoPair in MI:
        pair = infoPair[0] 
        if infoPair[1] <= threshold and pair[0] not in force and pair[1] not in force:
            continue
        Node0 = Nodes[pair[0]]
        Node1 = Nodes[pair[1]]
        if Ordering.index(pair[0]) > Ordering.index(pair[1]):
            S.append((Node1, Node0))
        else:
            S.append((Node0, Node1))
        
    Edges.append(S[0])
    Edges.append(S[1])
    
    R = []
    for e in S[2:]:
        if e[0] not in Nodes:
            Nodes.append(e[0])      
        if e[1] not in Nodes:
            Nodes.append(e[1])
        
        CI = condIndependence(e, Edges, [], Ordering)
        if CI:
            Edges.append(e)
        else:
            R.append(e)      
            
    for e in R:
        blockSet = findBlockSet(e, Edges, Ordering)
        if not condIndependence(e, Edges, blockSet, Ordering):
            Edges.append(e)
    
    tempEdge = Edges[:]
    i = 0
    while i < len(tempEdge):
        e = tempEdge[i]
        Edges.remove(e)
        path = findPath(e, Edges)
        if len(path) == 0:
            Edges.append(e)
            i = i + 1
        else:
            blockSet = findBlockSet(e, Edges, Ordering)
            if not condIndependence(e, Edges, blockSet, Ordering):
                Edges.append(e)
                i = i + 1
            else:
                tempEdge.remove(e)
    
    for e in Edges:
        parent = e[0]
        child = e[1]
        parent.addChild(child.id)
        child.addParent(parent.id)
        
    for n in Nodes:
        parentsid = n.parents
        parents = []
        for parent in Nodes:
            if parent.id in parentsid:
                parents.append(parent)
        
        recordedProb = n.values[:]
        
        recordedGiven = []
        if len(parents) > 0:
            recordedGiven = parents[0].values[:]   
            temp = recordedGiven[:]
            i = 1
            for parent in parents[1:]:
                recordedGiven = []
                for record in itertools.product(temp, parent.values):
                    if i == 1:
                        recordedGiven.append(record)
                    else:
                        temp2 = list(record[0])
                        temp2.append(record[1])
                        recordedGiven.append(tuple(temp2[:]))
                temp = recordedGiven[:]
                i = i+1
        
        cT = {}
        for p in recordedProb:
            if len(parents) > 0:
                for g in recordedGiven:
                    cT[(p, g)] = 0
            else:
                cT[p] = 0
                
        n.conditionalTable = cT
    return (Nodes, Edges, Ordering)
    
def MLE(Nodes, data):
    for n in Nodes:
        i = n.id
        parentIds = n.parents
        if len(parentIds) == 0:
            totalCount = 0
            valuesCount = {}
            for value in n.values:
                valuesCount[value] = 0
            for d in data[i]:
                if d in valuesCount:
                    valuesCount[d] = valuesCount[d] + 1                    
                    totalCount = totalCount + 1
                    
            for value in valuesCount:
                n.conditionalTable[value] = valuesCount[value]/totalCount
                
        else:
            parents = []
            for parent in Nodes:
                if parent.id in parentIds:
                    parents.append(parent)
            valuesCount = {}
            parentValuesCount = {}
            for value in n.conditionalTable:
                valuesCount[value] = 0
                parentValuesCount[value[1]] = 0
             
            for k in range(len(data[0])):
                temp = []
                for id in parentIds:
                    temp.append(data[id][k])
                pd = tuple(temp)
                if pd in parentValuesCount:
                    parentValuesCount[pd] = parentValuesCount[pd] + 1
                    v = data[i][k]
                    value = (v, pd)
                    if value in valuesCount:
                        valuesCount[value] = valuesCount[value] + 1
            
            for pv in parentValuesCount:
                for value in valuesCount:
                    if parentValuesCount[pv] > 0:
                        n.conditionalTable[(value, pv)] = valuesCount[value] / parentValuesCount[pv]              

def main():
    ### read data
    (variableNames, variableValues, data) = getData()
    storeData((variableNames, variableValues, data), 'Temp/data.pickle')
    ###
    

    ### Discretize the continuous variables, and shrink the size of categorical variables
    (variableNames, variableValues, data) = retrieveData('Temp/data.pickle')
    

    #getCategorialDataStat(variableValues, data)
    transCat = defineTransCat(variableNames, variableValues)
    translateCategorical(variableValues, data, transCat)
    getCategorialDataStat(variableValues, data)
    
    (transCont, unknown) = getTransCont(variableValues, data)
    translateContinuous(variableValues, data, unknown, transCont)
    
    storeData((variableNames, variableValues, data, transCont, transCat, unknown), 'Temp/data2.pickle')
    ###
    
    ### Build network
    (variableNames, variableValues, data, transCont, transCat, unknown) = retrieveData('Temp/data2.pickle')
    
    
    MI = mutualInformation(variableValues, data, unknown)
    sorted_MI = sorted(MI.items(), key=operator.itemgetter(1), reverse=True)
    
    storeData(sorted_MI, 'Temp/MI.pickle')
    
    sorted_MI = retrieveData('Temp/MI.pickle')
    (Nodes, Edges, Ordering) = buildTree(variableNames, variableValues, sorted_MI, rand=True)
    storeData((Nodes, Edges, Ordering), 'Temp/BN.pickle')
    
    (Nodes, Edges, Ordering) = retrieveData('Temp/BN.pickle')
    
    ###    
    #learn parameters
    MLE(Nodes, data)
    storeData(Nodes, 'Temp/final.pickle')
    
    print(Edges)
    
    print(Nodes[0].name, Nodes[0].conditionalTable)
    
    
    
    
#I(B,D) ≥ I(C,E) ≥ I(B,E) ≥ I(A,B) ≥ I(B,C) ≥ I(C,D) ≥ I(D,E) ≥ I(A,D) ≥ I(A,E) ≥ I(A,C)
def testmain():
    variableNames = ["A", "B", "C", "D", "E"]
    variableValues = [(0, 1), (0, 1), (0, 1), (0, 1), (0, 1)]
    sorted_MI = [((1, 3), 10), ((2, 4), 9), ((1, 4), 8), ((0, 1), 7), 
    ((1, 2), 6), ((2, 3), 5), ((3, 4), 4), ((0, 3), 3), ((0, 4), 2), ((0, 2), 1)]
    
    (Nodes, Edges, Ordering) = buildTree(variableNames, variableValues, sorted_MI)
    for edge in Edges:
        print(edge[0].name, "->", edge[1].name)
#testmain()
main()