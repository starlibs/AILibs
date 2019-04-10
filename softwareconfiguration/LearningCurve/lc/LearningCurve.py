import math


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
