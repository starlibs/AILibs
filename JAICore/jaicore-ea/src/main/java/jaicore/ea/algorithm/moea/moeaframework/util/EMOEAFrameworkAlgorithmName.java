package jaicore.ea.algorithm.moea.moeaframework.util;

public enum EMOEAFrameworkAlgorithmName {

	CMA_ES("CMA-ES"), DBEA("DBEA"), EMOEA("eMOEA"), ENSGAII("eNSGAII"), GDE3("GDE3"), IBEA("IBEA"), MOEAD("MOEAD"), MSOPS("MSOPS"), NSGAII("NSGAII"), NSGAIII("NSGAIII"), OMOPSO("OMOPSO"), PAES("PAES"), PESA2("PESA2"), RANDOM(
			"Random"), RVEA("RVEA"), SMPSO("SMPSO"), SMS_EMOA("SMS-EMOA"), SPEA2("SPEA2"), VEGA("VEGA"), GA("GA"), ES("ES"), DE("DE");

	private final String name;

	private EMOEAFrameworkAlgorithmName(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
