"""
Simple Plot to visualise stan output.
"""

import matplotlib.pyplot as plt
import numpy as np
import math
import csv
import os
import sys
import LearningCurve as lc

plotfile = "output.csv"
if len(sys.argv)  >= 2:
    plotfile = sys.argv[1]
    print(plotfile)

# remove comments from the csv file
def decomment(file):
    for row in file:
        t = row.split('#')[0].strip()
        if t: yield t

# samples read from stan output
samples = []
with open(os.path.join(os.path.dirname(__file__), plotfile),'r') as csvfile:
    plots = csv.reader(decomment(csvfile), delimiter=',')
    next(plots, None)
    i = 0
    for row in plots:
        samples.append(row)

x = []
y = []
comb = []
truth = []
s = 0

for i in range(1000):
    x.append((i+1)*2 +5)
    #original function for comparison, use same parameters as for generating the data (if u have)
    truth.append(lc.f_comb(1.0/6.0, 1.0/6.0, 1.0/6.0, 1.0/6.0, 1.0/6.0, 1.0/6.0, 0.35, -0.05, 1, 4.5, 0.5, 1.0, 4.0, -1.15, 0.04, 0, 1.2, 0.37, 1.1, -1.0, 0.6, 0.05, 0.1, 0.2, 1.0, 0.5, x[i]))
    comb.append(0)

for si in range(10):
    y.append([])
    #compute which samples are used to display
    s = si
    for i in range(1000):
        tmp = lc.f_combNew(float(samples[s][7]), float(samples[s][8]), float(samples[s][9]), float(samples[s][10]), float(samples[s][11]), float(samples[s][12]), float(samples[s][13]), float(samples[s][14]), float(samples[s][15]), float(samples[s][16]), float(samples[s][17]), float(samples[s][18]), float(samples[s][19]), float(samples[s][20]), float(samples[s][21]), float(samples[s][22]), float(samples[s][23]), float(samples[s][24]), float(samples[s][25]), float(samples[s][26]), float(samples[s][27]), float(samples[s][28]), float(samples[s][29]), float(samples[s][30]), float(samples[s][31]), float(samples[s][32]), x[i])
        if isinstance(tmp,complex):
            y[si].append(0)
        else:
            y[si].append(tmp)
            
        if tmp != None:
            comb[i] += tmp/10
    plt.plot(x,y[si], label="Curve-{}".format(si))


plt.plot(x,comb, 'b--', label="Curve-combined", linewidth=2 )

# commend in if u have a truth given
#plt.plot(x,truth, 'k--', label='truth', linewidth=2 )
plt.xlabel('x')
plt.ylabel('y')
plt.title('')
plt.legend()
plt.show()
