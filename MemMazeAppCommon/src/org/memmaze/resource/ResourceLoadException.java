package org.memmaze.resource;

public class ResourceLoadException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ResourceLoadException(String resourceType, Exception ex) {
		super(String.format("Failed to load resource of type '%s'.", resourceType), ex);
	}

}
