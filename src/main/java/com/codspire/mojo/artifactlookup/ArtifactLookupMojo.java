package com.codspire.mojo.artifactlookup;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * 
 *
 * @author Rakesh Nagar
 * @since 1.0
 */
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which searches local dependency jar/zip files in remote Maven repository
 * and translates them into respective Maven dependency coordinates (groupId,
 * artifactId, version).
 * 
 * @author Rakesh Nagar
 */

@Mojo(requiresProject = false, name = "lookup", defaultPhase = LifecyclePhase.NONE)
public class ArtifactLookupMojo extends AbstractMojo {

	/**
	 * Remote repositories resolved from effective settings. No need to manually
	 * set since it will be resolved from existing settings configurations.
	 */
	@Parameter(readonly = true, required = true, defaultValue = "${project.remoteArtifactRepositories}")
	protected List<ArtifactRepository> remoteArtifactRepositories;

	/**
	 * Specify the local directory containing the artifacts that need to be
	 * searched in remote Maven repositories. The plugin will look for jar/zip
	 * files in the "artifactLocation" directory and its sub-directories.
	 * 
	 * Alternatively, you can pass the path of a single artifact against that
	 * needs to be searched in remote Maven repositories.
	 * 
	 * This defaults to current directory where this plugin is run.
	 */
	@Parameter(readonly = false, required = true, property = "artifactLocation", defaultValue = ".")
	protected File artifactLocation;

	/**
	 * Look for sub directories for looking for artifacts, used only if
	 * "artifactLocation" is a directory.
	 * 
	 */
	@Parameter(readonly = false, required = false, property = "recursive", defaultValue = "true")
	protected boolean recursive;

	/**
	 * Optional property to specify remote Maven repository Url(s) if you want
	 * to search the artifacts in a repositories not configured in your
	 * settings.xml (This will supersede the remote repositories configured in
	 * Maven setttings.xml). Supports comma separated values.
	 * 
	 */
	@Parameter(readonly = false, required = false, property = "repositoryUrl")
	protected String repositoryUrl;

	/**
	 * Specify the output directory where the pom.xml dependencies snippet will
	 * be saved for the successful search results. the plugin will also generate
	 * a csv file to provide the useful information about the search status.
	 * 
	 * This defaults to current directory where this plugin is run.
	 */
	@Parameter(readonly = false, required = true, property = "outputDir", defaultValue = ".")
	protected File outputDirectory;

	/**
	 * Executes the artifact-lookup-maven-plugin:lookup
	 */
	public void execute() throws MojoExecutionException {
		lookupArtifacts();
	}

	/**
	 * Searches the local artifacts specified through "artifactLocation" in
	 * "remoteArtifactRepositories" or "repositoryUrl" and generates the search
	 * results.
	 * 
	 */
	protected void lookupArtifacts() {
		Log log = getLog();

		validateRemoteArtifactRepositories();
		validateArtifactLocation();

		List<String> remoteArtifactRepositoriesURL = getRemoteArtifactRepositoriesURL();

		log.info(artifactLocation.getAbsolutePath() + " is file = " + artifactLocation.isFile());

		if (log.isDebugEnabled()) {
			log.debug("Remote Artifact Repositories");
			log.debug((remoteArtifactRepositories != null) ? remoteArtifactRepositories.toString() : "remoteArtifactRepositories is null");
		}

		LookupForDependency lookupForDependency = new LookupForDependency(artifactLocation, recursive, remoteArtifactRepositoriesURL, outputDirectory, log);
		lookupForDependency.process();
	}

	/**
	 * Validates if the "artifactLocation" is correct.
	 */
	protected void validateArtifactLocation() {
		if (artifactLocation == null || !artifactLocation.exists()) {
			throw new ContextedRuntimeException("ERROR: artifactLocation property is invalid. Please provide -DartifactLocation=<file or folder path>");
		}
	}

	/**
	 * Validates if the remote Maven repositories information is available
	 * either through native settings.xml ("remoteArtifactRepositories") or
	 * through "repositoryUrl" parameter.
	 */
	protected void validateRemoteArtifactRepositories() {
		if (StringUtils.isBlank(repositoryUrl) && CollectionUtils.isEmpty(remoteArtifactRepositories)) {
			throw new ContextedRuntimeException("ERROR: No remote repository found, please check your settings.xml file or -DrepositoryUrl parameter.");
		}
	}

	/**
	 * Generates list of Urls resolved from "remoteArtifactRepositories" or
	 * "repositoryUrl", If "repositoryUrl" is specified it will supersede the
	 * "remoteArtifactRepositories"
	 * 
	 * @return List remote Maven repository Urls
	 */
	protected List<String> getRemoteArtifactRepositoriesURL() {

		List<String> remoteArtifactRepositoriesURLList = null;
		/* prefer repositoryUrl, it will supersede settings.xml */

		if (StringUtils.isNotBlank(repositoryUrl)) {
			getLog().info("Using repository: " + repositoryUrl);
			remoteArtifactRepositoriesURLList = Arrays.asList(repositoryUrl.split(","));
		} else {

			remoteArtifactRepositoriesURLList = new ArrayList<String>(remoteArtifactRepositories.size());

			for (ArtifactRepository artifactRepository : remoteArtifactRepositories) {
				remoteArtifactRepositoriesURLList.add(artifactRepository.getUrl());
			}
		}

		getLog().info("Repositories that will be searched: " + remoteArtifactRepositoriesURLList);
		return remoteArtifactRepositoriesURLList;
	}
}
