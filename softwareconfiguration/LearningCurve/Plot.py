import matplotlib.pyplot as plt
import math
import csv
import os

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

def f_comb(w_0, w_1, w_2, w_3, w_4, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, x):
	try:
		return w_0 * log_log_linear(a_0, b_0, x) + w_1 * pow_3(c_1, a_1, alpha_1, x) + w_2 * log_power(a_2, b_2, c_2, x) + w_3 * pow_4(a_3, b_3, c_3, alpha_3, x) + w_4 * mmf(alpha_4, beta_4, delta_4, kappa_4, x)
	except ValueError:
		print("Value error for x={}".format(x))


def decomment(csvfile):
    for row in csvfile:
        raw = row.split('#')[0].strip()
        if raw: yield raw

samples = []

with open(os.path.join(os.path.dirname(__file__), 'output.csv'),'r') as csvfile:
    plots = csv.reader(decomment(csvfile), delimiter=',')
    next(plots, None)
    i = 0
    for row in plots:
        samples.append(row)

x = []
y = []
truth = []
s = 0

for i in range(1000):
    x.append((i+1)*2)
    #original function for comparison, use same parameters as for generating the data
    truth.append(f_comb(1.0/5.0, 1.0/5.0, 1.0/5.0, 1.0/5.0, 1.0/5.0, 0.35, -0.05, 1, 4.5, 0.5, 1.0, 4.0, -1.15, 0.04, 0, 1.2, 0.37, 1.1, -1.0, 0.6, 0.05, x[i]))


for si in range(10):
    y.append([])
    #compute which samples are used to display
    s = si*100
    for i in range(1000):
        y[si].append(f_comb(float(samples[s][7]), float(samples[s][8]), float(samples[s][9]), float(samples[s][10]), float(samples[s][11]), float(samples[s][12]), float(samples[s][13]), float(samples[s][14]), float(samples[s][15]), float(samples[s][16]), float(samples[s][17]), float(samples[s][18]), float(samples[s][19]), float(samples[s][20]), float(samples[s][21]), float(samples[s][22]), float(samples[s][23]), float(samples[s][24]), float(samples[s][25]), float(samples[s][26]), float(samples[s][27]), x[i]))
    plt.plot(x,y[si], label="Curve-{}".format(si))


plt.plot(x,truth, 'k--', label='truth', linewidth=2 )

plt.xlabel('x')
plt.ylabel('y')
plt.title('')
plt.legend()
plt.show()
