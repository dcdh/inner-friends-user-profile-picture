package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import com.innerfriends.userprofilepicture.domain.usecase.*;
import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Objects;

@Path("/users")
public class UserProfilePictureEndpoint {

    private final SaveUserProfilePictureUseCase<Response> saveUserProfilePictureUseCase;
    private final GetLastUserProfilePictureUseCase<Response> getLastUserProfilePictureUseCase;

    private final SaveUserProfilePictureResponseTransformer<Response> saveUserProfilePictureResponseTransformer;
    private final GetLastUserProfilePictureResponseTransformer<Response> getLastUserProfilePictureResponseTransformer;

    public UserProfilePictureEndpoint(final SaveUserProfilePictureUseCase<Response> saveUserProfilePictureUseCase,
                                      final GetLastUserProfilePictureUseCase<Response> getLastUserProfilePictureUseCase) {
        this.saveUserProfilePictureUseCase = Objects.requireNonNull(saveUserProfilePictureUseCase);
        this.getLastUserProfilePictureUseCase = Objects.requireNonNull(getLastUserProfilePictureUseCase);
        this.saveUserProfilePictureResponseTransformer = new ResponseSaveUserProfilePictureResponseTransformer();
        this.getLastUserProfilePictureResponseTransformer = new ResponseGetLastUserProfilePictureResponseTransformer();
    }

    @POST
    @Path("/{userPseudo}/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Uni<Response> uploadUserProfilePicture(@PathParam("userPseudo") final String userPseudo,
                                                  @MultipartForm final UserProfilePicture userProfilePicture) {
        return saveUserProfilePictureUseCase.execute(
                new SaveUserProfilePictureCommand(new JaxRsUserPseudo(userPseudo), userProfilePicture.picture, userProfilePicture.mediaType),
                saveUserProfilePictureResponseTransformer);
    }

    @GET
    @Consumes("image/*")
    @Path("/{userPseudo}")
    public Uni<Response> downloadFile(@PathParam("userPseudo") final String userPseudo,
                                      @DefaultValue("image/jpeg; charset=ISO-8859-1") @HeaderParam("Content-Type") final String contentType) {
        return getLastUserProfilePictureUseCase.execute(new GetLastUserProfilePictureCommand(new JaxRsUserPseudo(userPseudo),
                        SupportedMediaType.fromContentType(
                                new ImageContentType(contentType).imageContentType())),
                getLastUserProfilePictureResponseTransformer);
    }

}
