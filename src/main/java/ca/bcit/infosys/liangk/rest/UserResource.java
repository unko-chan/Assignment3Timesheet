package ca.bcit.infosys.liangk.rest;

import ca.bcit.infosys.liangk.dto.CreateUserRequest;
import ca.bcit.infosys.liangk.dto.UpdateUserRequest;
import ca.bcit.infosys.liangk.dto.UserDTO;
import ca.bcit.infosys.liangk.entity.User;
import ca.bcit.infosys.liangk.entity.UserRole;
import ca.bcit.infosys.liangk.security.CurrentUserHolder;
import ca.bcit.infosys.liangk.service.UserService;
import ca.bcit.infosys.liangk.util.Mapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    private UserService userService;

    @Inject
    private CurrentUserHolder currentUserHolder;

    private void ensureAdmin() {
        User current = currentUserHolder.getUser();
        if (current == null || current.getRole() != UserRole.ADMIN) {
            throw new ca.bcit.infosys.liangk.exception.ForbiddenException("Admin privileges required");
        }
    }

    @GET
    public List<UserDTO> listUsers() {
        ensureAdmin();
        return userService.listAll().stream()
                .map(Mapper::toUserDTO)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    public UserDTO getUser(@PathParam("id") long id) {
        ensureAdmin();
        User u = userService.getById(id);
        return Mapper.toUserDTO(u);
    }

    // Test-friendly variant that avoids Response building dependency
    UserDTO createUserRaw(CreateUserRequest req) {
        ensureAdmin();
        User u = userService.createUser(req);
        return Mapper.toUserDTO(u);
    }

    @POST
    public Response createUser(CreateUserRequest req, @Context UriInfo uriInfo) {
        UserDTO dto = createUserRaw(req);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(dto.getId())).build();
        return Response.created(location).entity(dto).build();
    }

    @PUT
    @Path("/{id}")
    public UserDTO updateUser(@PathParam("id") long id, UpdateUserRequest req) {
        ensureAdmin();
        User updated = userService.updateUser(id, req);
        return Mapper.toUserDTO(updated);
    }

    // Test-friendly variant for delete to avoid Response builder
    void deleteUserRaw(long id) {
        ensureAdmin();
        userService.deleteUser(id);
    }

    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") long id) {
        deleteUserRaw(id);
        return Response.noContent().build();
    }
}
