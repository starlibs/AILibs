package ai.libs.jaicore.basic;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import ai.libs.jaicore.basic.sets.SetUtil;

public class ResourceFile extends File {

	/**
	 *
	 */
	private static final long serialVersionUID = -404232145050366072L;
	private final String pathName;

	public ResourceFile(final String pathname) {
		super(pathname);
		this.pathName = pathname;
	}

	public ResourceFile(final ResourceFile baseFile, final String pathname) {
		super(getPathName(baseFile, pathname));
		this.pathName = getPathName(baseFile, pathname);
	}

	private static String getPathName(final ResourceFile baseFile, final String pathName) {
		List<String> pathConstruct = new LinkedList<>();
		List<String> concatPaths = new LinkedList<>();

		concatPaths.addAll(SetUtil.explode(baseFile.getPathName(), "/"));
		concatPaths.addAll(SetUtil.explode(pathName, "/"));

		for (String pathElement : concatPaths) {
			if (pathElement.equals(".")) {
				continue;
			} else if (pathElement.equals("..")) {
				if (pathConstruct.isEmpty()) {
					throw new IllegalArgumentException("Cannot construct path from " + baseFile.getPathName() + " and " + pathName);
				} else {
					pathConstruct.remove(pathConstruct.size() - 1);
				}
			} else {
				pathConstruct.add(pathElement);
			}
		}

		return SetUtil.implode(pathConstruct, "/");
	}

	public InputStream getInputStream() {
		return this.getClass().getClassLoader().getResourceAsStream(this.pathName);
	}

	public final String getPathName() {
		return this.pathName;
	}

	@Override
	public final ResourceFile getParentFile() {
		List<String> stringList = SetUtil.explode(this.pathName, "/");
		if (stringList.size() >= 1) {
			stringList.remove(stringList.size() - 1);
			return new ResourceFile(SetUtil.implode(stringList, "/"));
		} else {
			return null;
		}
	}

	@Override
	public boolean exists() {
		return true;
	}

}
