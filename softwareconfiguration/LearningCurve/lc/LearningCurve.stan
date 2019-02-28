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
	
	real f_comb(real[] e, real x){
		return e[1] * log_log_linear(e[2], e[3], x) + 
			e[4] * pow_3(e[5], e[6], e[7], x) + 
			e[8] * log_power(e[9], e[10], e[11], x) + 
			e[12] * pow_4(e[13], e[14], e[15], e[16], x) + 
			e[17] * mmf(e[18], e[19], e[20], e[21], x) + 
			e[22] * exp_4(e[23], e[24], e[25], e[26], x);
	}
	
	real curveSlopeDist_log(real[] e, int c, int s){
		if(e[1] < 0)
			return log(0);
		if(e[4] < 0)
			return log(0);
		if(e[8] < 0)
			return log(0);
		if(e[12] < 0)
			return log(0);
		if(e[17] < 0)
			return log(0);
		if(e[22] < 0)
			return log(0);
		// additional constraints to get reasonable curves
		if(e[2] < 0)
			return log(0);
		if(e[6] < 0)
			return log(0);
		if(e[7] < 0)
			return log(0);
		if(e[9] > 0)
			return log(0);
		if(e[11] < 0)
			return log(0);
		if(e[13] < 0)
			return log(0);
		if(e[16] < 0)
			return log(0);
		if(e[20] < 0)
			return log(0);
		if(e[21] < 0)
			return log(0);
		if(f_comb(e, 10000) > 1)
			return log(0);
		for(k in 1:c)
			if(f_comb(e, k*s) > f_comb(e, s*k+s) )
				return log(0);
		return 0;
	}
}

data {
	int N;
	int c;
	int s;
	vector[N] x;
	vector[N] y;
}

parameters {
	real e[26];

// 0 	real<lower=0> w_0;
// 1 	real a_0;
// 2 	real b_0;
// 3 	real<lower=0> w_1;
// 4 	real c_1;
// 5 	real a_1;
// 6 	real alpha_1;
// 7 	real<lower=0> w_2;
// 8 	real a_2;
// 9 	real b_2;
//10	real c_2;
//11	real<lower=0> w_3;
//12	real a_3;
//13	real b_3;
//14	real c_3;
//15	real alpha_3;
//16	real<lower=0> w_4;
//17	real alpha_4;
//18	real beta_4;
//19	real delta_4;
//20	real kappa_4;
//21	real<lower=0> w_5;
//22	real a_5;
//23	real b_5;
//24	real c_5;
//25	real alpha_5;
	
	real<lower=0> sigma;
}

transformed parameters {
}

model {
	vector[N] ypred;
	//prior
	sigma ~ normal(0, 1);
	
	e ~ curveSlopeDist(c, s);
	
	// dist
	for (n in 1:N) 
		ypred[n] = f_comb(e, x[n]);
		//ypred[n] = log_log_linear(a_0, b_0, x[n]);
	y ~ normal(ypred, sigma);
}


