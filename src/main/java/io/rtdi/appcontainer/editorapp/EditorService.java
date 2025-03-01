package io.rtdi.appcontainer.editorapp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.rtdi.appcontainer.WebAppConstants;
import io.rtdi.appcontainer.realm.IAppContainerPrincipal;
import io.rtdi.appcontainer.utils.ErrorCode;
import io.rtdi.appcontainer.utils.ErrorMessage;
import io.rtdi.appcontainer.utils.SuccessMessage;
import io.rtdi.appcontainer.utils.Util;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/editorapp")
public class EditorService {
	protected final Logger log = LogManager.getLogger(this.getClass().getName());

	@Context
    private Configuration configuration;

	@Context 
	private ServletContext servletContext;
	
	@Context 
	private HttpServletRequest request;

	@GET
	@Path("file/{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
	@Operation(
			summary = "Content of a file",
			description = "Returns the content of the requested file",
			responses = {
					@ApiResponse(
	                    responseCode = "200",
	                    description = "The file content Json object",
	                    content = {
	                            @Content(
	                                    schema = @Schema(implementation = FileContent.class)
	                            )
	                    }
                    ),
					@ApiResponse(
							responseCode = "202", 
							description = "Any exception thrown",
		                    content = {
		                            @Content(
		                                    schema = @Schema(implementation = ErrorMessage.class)
		                            )
		                    }
					)
            })
	@Tag(name = "Filesystem")
    public Response getTextFileContent(
    		@PathParam("path")
   		 	@Parameter(
 	    		description = "Path in the format SCHEMA/dir1/dir2/fileX",
 	    		example = "SCHEMAXYZ/dir1/subdirA/fileX"
 	    		)
    		String path) {
		IAppContainerPrincipal user = (IAppContainerPrincipal) request.getUserPrincipal();
		try {
			String username = user.getDBUser();
			username = Util.validateFilename(username);
			java.nio.file.Path upath = WebAppConstants.getRepoUserDir(request.getServletContext(), username);
			File file = upath.resolve(path).toFile();
			if (!file.isFile()) {
				throw new IOException("Cannot find file \"" + file.getAbsolutePath() + "\" on the server");
			}
			return Response.ok(new FileContent(file, path)).build();
		} catch (Exception e) {
			return Response.status(Status.ACCEPTED).entity(new ErrorMessage(e, ErrorCode.LOWLEVELEXCEPTION)).build();
		}
	}

	@POST
	@Path("file/{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	@Operation(
			summary = "All directories of the user",
			description = "Returns a tree of all schemas and their directories of the currently logged in user",
			responses = {
					@ApiResponse(
	                    responseCode = "200",
	                    description = "A simple success message",
	                    content = {
	                            @Content(
	                                    schema = @Schema(implementation = SuccessMessage.class)
	                            )
	                    }
                    ),
					@ApiResponse(
							responseCode = "202", 
							description = "Any exception thrown",
		                    content = {
		                            @Content(
		                                    schema = @Schema(implementation = ErrorMessage.class)
		                            )
		                    }
					)
            })
	@Tag(name = "Filesystem")
    public Response writeTextFileContent(
    		@PathParam("path")
    		@Parameter(
 	    		description = "Path in the format SCHEMA/dir1/dir2/fileX",
 	    		example = "SCHEMAXYZ/dir1/subdirA/fileX"
 	    		)
    		String path,
    		@Parameter(
 	    		description = "payload of the file",
 	    		example = "This is a free form text"
 	    		)
    		String content) {
		IAppContainerPrincipal user = (IAppContainerPrincipal) request.getUserPrincipal();
		try {
			String username = user.getDBUser();
			username = Util.validateFilename(username);
			java.nio.file.Path upath = WebAppConstants.getRepoUserDir(request.getServletContext(), username);
			java.nio.file.Path ppath = upath.resolve(path);
			if (!ppath.toFile().isFile()) {
				throw new IOException("Cannot find file \"" + ppath.toAbsolutePath().toString() + "\" on the server");
			}
			Files.writeString(ppath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			return Response.ok(new SuccessMessage(path)).build();
		} catch (Exception e) {
			return Response.status(Status.ACCEPTED).entity(new ErrorMessage(e, ErrorCode.LOWLEVELEXCEPTION)).build();
		}
	}

	@Schema(description="Json structure with the file content information")
	public static class FileContent {
		private String content;
		private String filename;
		private String path;
		
		public FileContent() {
			super();
		}

		public FileContent(File file, String path) throws IOException {
			this.path = path;
			this.filename = file.getName();
			content = new String(Files.readAllBytes(file.toPath()));
		}

		public String getContent() {
			return content;
		}

		public String getFilename() {
			return filename;
		}

		public String getPath() {
			return path;
		}
	}
}
