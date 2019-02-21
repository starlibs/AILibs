functions{
	
	real log_log_linear(real a, real b, real x){
		return log(a * log(x) + b);
	}
	
	real pow_3(real c, real a, real alpha, real x){
		return c - a * (x^(-alpha));
	}

	real log_power(real a, real b, real c, real x){
		return a / (1 + (x/exp(b))^c);
	}

	real pow_4(real a, real b, real c, real alpha, real x){
		return c - (a * x + b)^(-alpha);
	}

	real mmf(real alpha, real beta, real delta, real kappa, real x){
		return alpha - (alpha - beta)/(1 + (kappa * x)^delta);
	}

	real exp_4(real a, real b, real c, real alpha, real x){
		return c - exp(-a * x^alpha + b);
	}
	
	real f_comb(real w_0, real w_1, real w_2, real w_3, real w_4, real w_5, real a_0, real b_0, real c_1, real a_1, real alpha_1, real a_2, real b_2, real c_2, real a_3, real b_3, real c_3, real alpha_3, real alpha_4, real beta_4, real delta_4, real kappa_4, real a_5, real b_5, real c_5, real alpha_5, real x){
		return w_0 * log_log_linear(a_0, b_0, x) + w_1 * pow_3(c_1, a_1, alpha_1, x) + w_2 * log_power(a_2, b_2, c_2, x) + w_3 * pow_4(a_3, b_3, c_3, alpha_3, x) + w_4 * mmf(alpha_4, beta_4, delta_4, kappa_4, x) + w_5 * exp_4(a_5, b_5, c_5, alpha_5, x);
	}
	
	real curveSlopeDist_log(real tmp, real w_0, real w_1, real w_2, real w_3, real w_4, real w_5, real a_0, real b_0, real c_1, real a_1, real alpha_1, real a_2, real b_2, real c_2, real a_3, real b_3, real c_3, real alpha_3, real alpha_4, real beta_4, real delta_4, real kappa_4, real a_5, real b_5, real c_5, real alpha_5, int m){
		for(k in 1:5)
			if(f_comb(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, k*400) > f_comb(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, 400*k + 400) )
				return 0;
		return 1;
	}
}

data {
	int N;
	int m;
	vector[N] x;
	vector[N] y;
}

parameters {
	
	real<lower=0> w_0;
	real a_0;
	real b_0;
	real<lower=0> w_1;
	real c_1;
	real a_1;
	real alpha_1;
	real<lower=0> w_2;
	real a_2;
	real b_2;
	real c_2;
	real<lower=0> w_3;
	real a_3;
	real b_3;
	real c_3;
	real alpha_3;
	real<lower=0> w_4;
	real alpha_4;
	real beta_4;
	real delta_4;
	real kappa_4;
	real<lower=0> w_5;
	real a_5;
	real b_5;
	real c_5;
	real alpha_5;
	
	real<lower=0> sigma;
}

transformed parameters {
}

model {
	vector[N] ypred;
	//prior
	w_0 ~ normal(1,1);
	w_1 ~ normal(1,1);
	w_2 ~ normal(1,1);
	w_3 ~ normal(1,1);
	w_4 ~ normal(1,1);
	w_5 ~ normal(1,1);
	sigma ~ normal(0, 1);
	
	a_0 ~ curveSlopeDist(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, m);
	b_0 ~ curveSlopeDist(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, m);
	c_1 ~ curveSlopeDist(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, m);
	a_1 ~ curveSlopeDist(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, m);
	alpha_1 ~ curveSlopeDist(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, m);
	a_2 ~ curveSlopeDist(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, m);
	b_2 ~ curveSlopeDist(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, m);
	c_2 ~ curveSlopeDist(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, m);
	a_3 ~ curveSlopeDist(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, m);
	b_3 ~ curveSlopeDist(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, m);
	c_3 ~ curveSlopeDist(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, m);
	alpha_3 ~ curveSlopeDist(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, m);
	alpha_4 ~ curveSlopeDist(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, m);
	beta_4 ~ curveSlopeDist(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, m);
	delta_4 ~ curveSlopeDist(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, m);
	kappa_4 ~ curveSlopeDist(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, m);
	a_5 ~ curveSlopeDist(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, m);
	b_5 ~ curveSlopeDist(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, m);
	c_5 ~ curveSlopeDist(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, m);
	alpha_5 ~ curveSlopeDist(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, m);
	
	a_0 ~ normal(0.5,0.5);
	b_0 ~ normal(1,1);
	c_1 ~ normal(1, 1);
	a_1 ~ normal(10, 5);
	alpha_1 ~ normal(0.5,0.5);
	a_2 ~ normal(1,0.2);
	b_2 ~ normal(5, 2);
	c_2 ~ normal(-1,0.5);
	a_3 ~ normal(0.2,0.1);
	b_3 ~ normal(0,1);
	c_3 ~ normal(1,0.3);
	alpha_3 ~ normal(0.25,0.25);
	alpha_4 ~ normal(1.5, 0.5);
	beta_4 ~ normal(-1,1);
	delta_4 ~ normal(0.5,0.25);
	kappa_4 ~ normal(0.5,0.25);
	a_5 ~ normal(0.1,0.05);
	b_5 ~ normal(0,0.5);
	c_5 ~ normal(1,0.3);
	alpha_5 ~ normal(0.5,0.2);
	
	
	
	// dist
	for (n in 1:N) 
		ypred[n] = f_comb(w_0, w_1, w_2, w_3, w_4, w_5, a_0, b_0, c_1, a_1, alpha_1, a_2, b_2, c_2, a_3, b_3, c_3, alpha_3, alpha_4, beta_4, delta_4, kappa_4, a_5, b_5, c_5, alpha_5, x[n]);
		//ypred[n] = log_log_linear(a_0, b_0, x[n]);
	y ~ normal(ypred, sigma);
}


