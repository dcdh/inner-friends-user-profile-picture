package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.ResponseTransformer;
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
    private final GetFeaturedUserProfilePictureUseCase<Response> getFeaturedUserProfilePictureUseCase;

    private final ResponseTransformer<Response> jaxRsResponseTransformer;

    public UserProfilePictureEndpoint(final SaveUserProfilePictureUseCase<Response> saveUserProfilePictureUseCase,
                                      final GetFeaturedUserProfilePictureUseCase<Response> getFeaturedUserProfilePictureUseCase) {
        this.saveUserProfilePictureUseCase = Objects.requireNonNull(saveUserProfilePictureUseCase);
        this.getFeaturedUserProfilePictureUseCase = Objects.requireNonNull(getFeaturedUserProfilePictureUseCase);
        this.jaxRsResponseTransformer = new JaxRsResponseTransformer();
    }

    @POST
    @Path("/{userPseudo}/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Uni<Response> uploadUserProfilePicture(@PathParam("userPseudo") final String userPseudo,
                                                  @MultipartForm final UserProfilePicture userProfilePicture) {
        return saveUserProfilePictureUseCase.execute(
                new SaveUserProfilePictureCommand(new JaxRsUserPseudo(userPseudo), userProfilePicture.picture, userProfilePicture.mediaType),
                jaxRsResponseTransformer);
    }

    @GET
    @Consumes("image/*")
    @Path("/{userPseudo}/featured")
    public Uni<Response> downloadFeaturedUserProfilePicture(@PathParam("userPseudo") final String userPseudo,
                                                            @DefaultValue("image/jpeg; charset=ISO-8859-1") @HeaderParam("Content-Type") final String contentType) {
        return getFeaturedUserProfilePictureUseCase.execute(new GetFeaturedUserProfilePictureCommand(new JaxRsUserPseudo(userPseudo),
                        SupportedMediaType.fromContentType(
                                new ImageContentType(contentType).imageContentType())),
                jaxRsResponseTransformer);
    }

}
