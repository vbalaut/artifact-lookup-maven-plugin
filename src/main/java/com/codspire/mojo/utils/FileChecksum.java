package com.codspire.mojo.utils;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

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

//http://memorynotfound.com/calculate-file-checksum-java/
//http://choosealicense.com/
//TODO: use the implementation from http://omtlab.com/how-to-generate-md5-and-sha1-checksum-in-java/
//TODO: remove maven build warning

/**
 * 
 *
 * @author Rakesh Nagar
 * @since 1.0
 */
public class FileChecksum {

	public static String generateSHA1Checksum(File file) {
		FileInputStream fis = null;
		String sha1 = null;
		try {
			fis = new FileInputStream(file);
			sha1 = DigestUtils.sha1Hex(fis);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(fis);
		}
		return sha1;
	}
}

/*
 * enum Hash {
 * 
 * MD5("MD5"), SHA1("SHA1"), SHA256("SHA-256"), SHA512("SHA-512");
 * 
 * private String name;
 * 
 * Hash(String name) { this.name = name; }
 * 
 * public String getName() { return name; } }
 */