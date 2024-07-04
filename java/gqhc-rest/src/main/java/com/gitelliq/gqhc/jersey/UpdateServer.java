package com.gitelliq.gqhc.jersey;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.gitelliq.gqhc.updates.MachineInfo;
import com.gitelliq.gqhc.updates.Plugin;
import com.gitelliq.gqhc.updates.Update;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path("updates")
public class UpdateServer {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postUpdate(@Context HttpServletRequest request, MachineInfo info) throws IOException {

		System.out.println("Remote system: " + info.getSystem());
		System.out.println("Remote release: " + info.getRelease());
		System.out.println("Remote version: " + info.getVersion());
		System.out.println("Remote machine: " + info.getMachine());
		System.out.println("Remote machine ID: " + info.getMachineID());
		System.out.println("Remote GQ hash: " + info.getGqHash());
		System.out.println("Remote GQ release: " + info.getGqRelease());

		
		String updateBaseDir = System.getProperty("gq.updates.store");
		System.out.println("Updates stored in " + updateBaseDir);
		String updateBaseURL = System.getProperty("gq.updates.baseurl");
		System.out.println("Base URL for update downloads is " + updateBaseURL);
		
		String relativeUpdateDir = info.getSystem() + "/" + info.getMachine() +"/" + info.getRelease() + "/" + info.getGqRelease();
		String updateDir = updateBaseDir + "/" + relativeUpdateDir;
		System.out.println("Checking " + updateDir + " for updates");
		
		String md5;
		
		/*
		{ 
				
			FileInputStream fis = new FileInputStream(new File(updateDir + "/" + "libgqdl.so"));
			md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
			fis.close();
		} 
		
		System.out.println("MD5: " + md5);
		*/
		List<Update> updates = new Vector<Update>();
		/*
		if (!md5.equalsIgnoreCase(info.getGqHash())) {
			System.out.println("Hash codes differ - requesting update of libgqdl.so");
			Update update = new Update();
			update.setUrl("http://10.2.111.4:8080/artifacts/" + relativeUpdateDir + "/libgqdl.so");
			update.setHash(md5);
			update.setFilename("./libgqdl.so");
			updates.add(update);
		} else {
			System.out.println("Identical hash code - omitting update of libgqdl.so");
		}
		*/
		
		Plugin[] plugins =  info.getPlugins();
		for (int i = 0; i < plugins.length; i++) {
			String pp = updateDir + "/" + plugins[i].getPath();
			System.out.println("Checking plugin " + pp);
			try {
				FileInputStream fis = new FileInputStream(new File(pp));
				md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
				if (!md5.equalsIgnoreCase(plugins[i].getHash())) {
					System.out.println("Update for plugin, MD5: " + md5);
					Update update = new Update();
					update.setUrl(updateBaseURL + "/" + relativeUpdateDir + "/" + plugins[i].getPath());
					update.setHash(md5);
					update.setFilename(plugins[i].getPath());
					updates.add(update);
				} else {
					System.out.println("Client already has latest version of: " + plugins[i].getPath());
				}
				fis.close();
			} catch(FileNotFoundException e) {
				System.err.println("No update for plugin: " + pp);
			}
		}
		
		return Response.status(Response.Status.OK).entity(updates).build();
	}
}
