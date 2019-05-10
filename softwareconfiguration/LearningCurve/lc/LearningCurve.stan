// This stan model fits the given data to a linear combination of curves. 
// The curves were choosen in such a way that they fulfill our expectations for
// meaningfull learning curves.
// This is done in a similar way to the paper: 
// "Speeding up Automatic Hyperparameter Optimization of Deep Neural Networksby Extrapolation of Learning Curves"
// by Tobias Domhan, Jost Tobias Springenberg and Frank Hutter
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
		if(e[2] < 0) // a
			return log(0);
		if(e[6] < 0) // a
			return log(0);
		if(e[7] < 0) // alpha
			return log(0);
		if(e[9] > 0) // a
			return log(0);
		if(e[11] < 0) // c
			return log(0);
		if(e[13] < 0) // a
			return log(0);
		if(e[16] < 0) // alpha
			return log(0);
		if(e[20] < 0) // delta
			return log(0);
		if(e[21] < 0) // kappa
			return log(0);
		// performance can never be better than 1
		if(f_comb(e, 10000) > 1)
			return log(0);
		// we only want non-decreasing learning curves
		for(k in 1:c)
			if(f_comb(e, k*s) > f_comb(e, s*k+s) )
				return log(0);
		return 0;
	}
}

data {
	// number of input points
	int N;
	
	// number of times the non-decreasing property is checked
	int c;
	// distance between the checks for non-decreasing property
	int s;
	
	// input points x 
	vector[N] x;
	// input points y
	vector[N] y;
}

parameters {
	real e[26];	
	real<lower=0> sigma;
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
