package ca.bcit.infosys.liangk.rest;

import ca.bcit.infosys.liangk.dto.TimesheetDTO;
import ca.bcit.infosys.liangk.entity.Timesheet;
import ca.bcit.infosys.liangk.entity.User;
import ca.bcit.infosys.liangk.security.CurrentUserHolder;
import ca.bcit.infosys.liangk.service.TimesheetService;
import ca.bcit.infosys.liangk.util.Mapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/timesheets")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TimesheetResource {

    @Inject
    private TimesheetService timesheetService;

    @Inject
    private CurrentUserHolder currentUserHolder;

    private User current() {
        return currentUserHolder.getUser();
    }

    @GET
    public List<TimesheetDTO> list(@QueryParam("weekStart") String weekStart) {
        Optional<LocalDate> weekOpt = Optional.empty();
        if (weekStart != null && !weekStart.isBlank()) {
            weekOpt = Optional.of(LocalDate.parse(weekStart));
        }
        List<Timesheet> ts = timesheetService.listTimesheets(current(), weekOpt);
        return ts.stream().map(Mapper::toTimesheetDTO).collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    public TimesheetDTO get(@PathParam("id") long id) {
        Timesheet t = timesheetService.getTimesheet(current(), id);
        return Mapper.toTimesheetDTO(t);
    }

    @POST
    public Response create(TimesheetDTO dto, @Context UriInfo uriInfo) {
        Timesheet created = timesheetService.createTimesheet(current(), dto);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.getId())).build();
        return Response.created(location).entity(Mapper.toTimesheetDTO(created)).build();
    }

    @PUT
    @Path("/{id}")
    public TimesheetDTO update(@PathParam("id") long id, TimesheetDTO dto) {
        Timesheet updated = timesheetService.updateTimesheet(current(), id, dto);
        return Mapper.toTimesheetDTO(updated);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") long id) {
        timesheetService.deleteTimesheet(current(), id);
        return Response.noContent().build();
    }
}
