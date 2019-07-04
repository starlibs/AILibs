package ai.libs.jaicore.basic;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ai.libs.jaicore.basic.sets.SetUtil;

/**
 * ResourceFile may be used to encapsulate files to be loaded from resources, i.e. from the build directory or from inside of a jar.
 *
 * @author mwever
 */
public class ResourceFile extends File {

	/**
	 *
	 */
	private static final long serialVersionUID = -404232145050366072L;

	/* The path to the resource */
	private final String pathName;

	/**
	 * C'tor for instantiating a resource file.
	 * @param pathname The path to the resource
	 */
	public ResourceFile(final String pathname) {
		super(pathname);
		this.pathName = pathname;
	}

	/**
	 * C'tor for instantiating a resource file relative to another resource file.
	 * @param baseFile The base file to which the relative path is to be considered.
	 * @param pathname The relative path to the resource with respect to the base file.
	 */
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
				// do nothing
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

	/**
	 * Getter for an input stream to read this resource file.
	 * @return The input stream to read the contents of this resource file.
	 */
	public InputStream getInputStream() {
		return this.getClass().getClassLoader().getResourceAsStream(this.pathName);
	}

	/**
	 * Getter for the resource's path.
	 * @return The path of the resource file.
	 */
	public final String getPathName() {
		return this.pathName;
	}

	@Override
	public final String getPath() {
		return this.getPathName();
	}

	@Override
	public final ResourceFile getParentFile() {
		List<String> stringList = SetUtil.explode(this.pathName, "/");
		if (!stringList.isEmpty()) {
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

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof ResourceFile)) {
			return false;
		}
		ResourceFile other = (ResourceFile) obj;
		return new EqualsBuilder().append(this.pathName, other.pathName).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.pathName).toHashCode();
	}

}
