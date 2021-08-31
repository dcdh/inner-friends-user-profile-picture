package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

public class UserProfilePicture {

    @FormParam("picture")
    @Schema(type = SchemaType.STRING, format = "binary", description = "content data")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] picture;

    @FormParam("supportedMediaType")
    @PartType(MediaType.TEXT_PLAIN)
    public SupportedMediaType mediaType;

}
