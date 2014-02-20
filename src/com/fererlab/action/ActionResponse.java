package com.fererlab.action;

import com.fererlab.dto.Request;
import com.fererlab.dto.Response;
import com.fererlab.dto.ServerResponse;
import com.fererlab.dto.Status;

/**
 * acm
 */
public abstract class ActionResponse {

    public class PrepareResponse {
        private Request request;
        private Status status;
        private ServerResponse serverResponse;

        public PrepareResponse(Request request, Status status, ServerResponse serverResponse) {
            this.request = request;
            this.status = status;
            this.serverResponse = serverResponse;
        }

        public PrepareResponse add(String key, Object value) {
            serverResponse.add(key, value);
            return this;
        }

        public Response toResponse() {
            return Response.create(request, toContent(request, serverResponse), status);
        }
    }

    public abstract String toContent(Request request, Object... objects);

    private PrepareResponse action(Request request, String[] messageAndStatus, Status st) {
        String message = messageAndStatus.length > 0 ? messageAndStatus[0] != null ? messageAndStatus[0] : "" : "";
        String status = messageAndStatus.length > 1 ? messageAndStatus[0] != null ? messageAndStatus[0] : "success" : "success";
        return new PrepareResponse(request, st, new ServerResponse(status, message));
    }

    public PrepareResponse Ok(Request request, String... messageAndStatus) {
        return action(request, messageAndStatus, Status.STATUS_OK);
    }

    public PrepareResponse BadRequest(Request request, String... messageAndStatus) {
        return action(request, messageAndStatus, Status.STATUS_BAD_REQUEST);
    }

    public PrepareResponse Unauthorized(Request request, String... messageAndStatus) {
        return action(request, messageAndStatus, Status.STATUS_UNAUTHORIZED);
    }

    public PrepareResponse NoContent(Request request, String... messageAndStatus) {
        return action(request, messageAndStatus, Status.STATUS_NO_CONTENT);
    }

    public PrepareResponse NotFound(Request request, String... messageAndStatus) {
        return action(request, messageAndStatus, Status.STATUS_NOT_FOUND);
    }

    public PrepareResponse NotModified(Request request, String... messageAndStatus) {
        return action(request, messageAndStatus, Status.STATUS_NOT_MODIFIED);
    }

    public PrepareResponse Redirect(Request request, String... messageAndStatus) {
        return action(request, messageAndStatus, Status.STATUS_TEMPORARY_REDIRECT);
    }

    public PrepareResponse RedirectToStatic(Request request, String... messageAndStatus) {
        return action(request, messageAndStatus, Status.STATUS_PERMANENT_REDIRECT);
    }

    public PrepareResponse Error(Request request, String... messageAndStatus) {
        return action(request, messageAndStatus, Status.STATUS_INTERNAL_SERVER_ERROR);
    }

    public PrepareResponse Forbidden(Request request, String... messageAndStatus) {
        return action(request, messageAndStatus, Status.STATUS_FORBIDDEN);
    }

}
