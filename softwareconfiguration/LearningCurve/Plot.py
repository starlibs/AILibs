import matplotlib.pyplot as plt
import math
import csv

def log_log_linear(a, b, x):
	return math.log(a * math.log(x) + b)
	
def pow_3(c, a, alpha, x):
	return c - a * (x**(-alpha))

	
def f_comb(w_0, w_1, a_0, b_0, c_1, a_1, alpha_1, x):
	return w_0 * log_log_linear(a_0, b_0, x) + w_1 * pow_3(c_1, a_1, alpha_1, x)


def decomment(csvfile):
    for row in csvfile:
        raw = row.split('#')[0].strip()
        if raw: yield raw

samples = []

# relative path does not work for some reason
with open('D://Software/Cmdstan-2.18.0/examples/LearningCurve/output.csv','r') as csvfile:
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
    truth.append(f_comb(1, 0, 2, 1, 1, 1, 0.5, x[i]))
            

for si in range(10):
    y.append([])
    #compute which samples are used to display
    s = si*40
    for i in range(1000):
        y[si].append(f_comb(float(samples[s][7]), float(samples[s][8]), float(samples[s][9]), float(samples[s][10]), float(samples[s][11]), float(samples[s][12]), float(samples[s][13]), x[i]))
    plt.plot(x,y[si], label='Curve')

plt.plot(x,truth, 'k--', label='truth', linewidth=2 )

plt.xlabel('x')
plt.ylabel('y')
plt.title('')
plt.legend()
plt.show()