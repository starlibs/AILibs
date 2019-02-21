import matplotlib.pyplot as plt
import numpy as np
import math
import csv
import os
import sys

plotfile = "output.csv"
if len(sys.argv)  >= 2:
    plotfile = sys.argv[1]
    print(plotfile)

def log_log_linear(a, b, x):
	return math.log(a * math.log(x) + b)

def pow_3(c, a, alpha, x):
	return c - a * (x**(-alpha))

def log_power(a,b,c,x):
	return a / (1 + (x/math.exp(b))**c)

def pow_4(a,b,c,alpha,x):
	return c - (a * x + b)**(-alpha)

def mmf(alpha, beta, delta, kappa, x):
	return alpha - (alpha - beta)/(1 + (kappa * x)**delta)

def exp_4(a,b,c,alpha,x):
	return c - math.exp(-a * x**alpha + b)

def f_comb(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, x):
	try:
		return w_0 * log_log_linear(a_0, b_0, x) + w_1 * pow_3(c_1, a_1, alpha_1, x) + w_2 * log_power(a_2, b_2, c_2, x) + w_3 * pow_4(a_3, b_3, c_3, alpha_3, x) + w_4 * mmf(alpha_4, beta_4, delta_4, kappa_4, x) + w_5 * exp_4(a_5, b_5, c_5, alpha_5, x)
	except ValueError:
		print("Value error for x={}".format(x))

def f_combNew(w_0, a_0, b_0, w_1, c_1, a_1, alpha_1, w_2, a_2, b_2, c_2, w_3, a_3, b_3, c_3, alpha_3, w_4, alpha_4, beta_4, delta_4, kappa_4, w_5, a_5, b_5, c_5, alpha_5, x):
	try:
		return w_0 * log_log_linear(a_0, b_0, x) + w_1 * pow_3(c_1, a_1, alpha_1, x) + w_2 * log_power(a_2, b_2, c_2, x) + w_3 * pow_4(a_3, b_3, c_3, alpha_3, x) + w_4 * mmf(alpha_4, beta_4, delta_4, kappa_4, x) + w_5 * exp_4(a_5, b_5, c_5, alpha_5, x)
	except ValueError:
		print("Value error for x={}".format(x))


def decomment(csvfile):
    for row in csvfile:
        raw = row.split('#')[0].strip()
        if raw: yield raw



def generateData(name):
    x = []
    y = np.random.normal(0,0.3,10)
    

    for i in range(10):
        x.append(10*(i+1))
        #original function for comparison, use same parameters as for generating the data
        y[i] += (f_comb(1.0/6.0, 1.0/6.0, 1.0/6.0, 1.0/6.0, 1.0/6.0, 1.0/6.0, 0.35, -0.05, 1, 4.5, 0.5, 1.0, 4.0, -1.15, 0.04, 0, 1.2, 0.37, 1.1, -1.0, 0.6, 0.05, 0.1, 0.2, 1.0, 0.5, x[i]))

    with open(os.path.join(os.path.dirname(__file__), name), 'w') as f:
        f.write("N <- " + str(len(x)) + "\n")
        f.write("x <- c(" + str(x).strip('[]') + ")\n" )
        f.write("y <- c(" + str(y).strip('[]') + ")\n" )


def plot():
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
        #original function for comparison, use same parameters as for generating the data
        truth.append(f_comb(1.0/6.0, 1.0/6.0, 1.0/6.0, 1.0/6.0, 1.0/6.0, 1.0/6.0, 0.35, -0.05, 1, 4.5, 0.5, 1.0, 4.0, -1.15, 0.04, 0, 1.2, 0.37, 1.1, -1.0, 0.6, 0.05, 0.1, 0.2, 1.0, 0.5, x[i]))
        comb.append(0)

    for si in range(10):
        y.append([])
        #compute which samples are used to display
        s = si
        for i in range(1000):
            tmp = f_combNew(float(samples[s][7]), float(samples[s][8]), float(samples[s][9]), float(samples[s][10]), float(samples[s][11]), float(samples[s][12]), float(samples[s][13]), float(samples[s][14]), float(samples[s][15]), float(samples[s][16]), float(samples[s][17]), float(samples[s][18]), float(samples[s][19]), float(samples[s][20]), float(samples[s][21]), float(samples[s][22]), float(samples[s][23]), float(samples[s][24]), float(samples[s][25]), float(samples[s][26]), float(samples[s][27]), float(samples[s][28]), float(samples[s][29]), float(samples[s][30]), float(samples[s][31]), float(samples[s][32]), x[i])
            if isinstance(tmp,complex):
                y[si].append(0)
            else:
                y[si].append(tmp)
                
            if tmp != None:
                comb[i] += tmp/10
        plt.plot(x,y[si], label="Curve-{}".format(si))

    
    plt.plot(x,comb, 'b--', label="Curve-combined", linewidth=2 )
   
    #plt.plot(x,truth, 'k--', label='truth', linewidth=2 )
    plt.xlabel('x')
    plt.ylabel('y')
    plt.title('')
    plt.legend()
    plt.show()

def testPlot():
    x = []
    y = []
    
    for i in range(1000):
        x.append((i+1)*10 +5)
        #original function for comparison, use same parameters as for generating the data
        #y.append(f_comb(1.0/6.0, 1.0/6.0, 1.0/6.0, 1.0/6.0, 1.0/6.0, 1.0/6.0, 0.35, -0.04, 1, 4.5, 1, 1.0, 3.0, -1.5, 0.04, 0, 0.6, 0.37, 1.1, -1.0, 0.5, 0.05, 0.1, 0.2, 0.9, 0.5, x[i]))
        #y.append(log_log_linear(0.497858, 0.867087, x[i]))
        #y.append(pow_3(3.24848, 4.62609, 1.22915, x[i]))
        y.append(log_power(-9.6865, 4.61269, 1.48929, x[i]))
        #y.append(log_log_linear(0.497858, 0.867087, x[i]))
        #y.append(log_log_linear(0.497858, 0.867087, x[i]))
        

    plt.plot(x,y, 'k--', label='truth', linewidth=2 )
    plt.xlabel('x')
    plt.ylabel('y')
    plt.title('')
    plt.legend()
    plt.show()



#testPlot()

plot()
#generateData("TestData7.data.R")