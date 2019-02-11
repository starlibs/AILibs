functions{
	
	real log_log_linear(real a, real b, real x){
		return log(a * log(x) + b);
	}
	
	real pow_3(real c, real a, real alpha, real x){
		return c - a * (x^(-alpha));
	}
	
	real f_comb(real w_0, real w_1, real a_0, real b_0, real c_1, real a_1, real alpha_1, real x){
		return w_0 * log_log_linear(a_0, b_0, x) + w_1 * pow_3(c_1, a_1, alpha_1, x);
	}
	
}

data {
	int N;
	vector[N] x;
	vector[N] y;
}

parameters {
	real<lower=0> w_0;
	real<lower=0> w_1;
	real a_0;
	real b_0;
	real c_1;
	real a_1;
	real alpha_1;
	
	real<lower=0> sigma;
}

transformed parameters {
}

model {
	vector[N] ypred;
	//prior
	sigma ~ normal(0, 1);
	c_1 ~ normal(1, 1);
	a_1 ~ normal(1, 1);
	
	// dist
	for (n in 1:N) 
		ypred[n] = f_comb(w_0, w_1, a_0, b_0, c_1, a_1, alpha_1, x[n]);
		//ypred[n] = log_log_linear(a_0, b_0, x[n]);
	y ~ normal(ypred, sigma);
}


