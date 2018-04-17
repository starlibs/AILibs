package de.upb.crc901.mlplan.scc2018;

import java.io.File;

import de.upb.crc901.mlplan.multiclass.core.MLUtil;

public class SHOP2Tester {
	public static void main(String[] args) {
		System.out.println(MLUtil.getJSHOP2File(new File("testrsc/services/automl-services.searchspace")).getY());
	}
}
