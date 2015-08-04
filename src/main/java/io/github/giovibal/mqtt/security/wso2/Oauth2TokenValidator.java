package io.github.giovibal.mqtt.security.wso2;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.jaxws.description.xml.handler.ObjectFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationService;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationServiceStub;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by giova_000 on 12/02/2015.
 */
public class Oauth2TokenValidator {
    private static String adminUserName;
    private static String adminPassword;

    private OAuth2TokenValidationService oAuth2TokenValidationService = null;
    private ObjectFactory objectFactory;

    public static void main(String[] args) throws Exception {
        String identityURL = "http://192.168.231.55:9763";
        String idp_userName="admin";
        String idp_password="d0_ut_d3s$";
        Oauth2TokenValidator oauth2Validator = new Oauth2TokenValidator(identityURL, idp_userName, idp_password);

        System.out.println(oauth2Validator.tokenIsValid("163894acdc8eedd8c06fb1227acfb350"));
        System.out.println(oauth2Validator.getTokenInfo("163894acdc8eedd8c06fb1227acfb350"));
    }


    public Oauth2TokenValidator(String identityURL, String userName, String password)
            throws MalformedURLException, AxisFault {

        adminUserName = userName;
        adminPassword = password;

        objectFactory = new ObjectFactory();

        //create service client with given url
        String targetEndpointUrl = identityURL + "/services/OAuth2TokenValidationService.OAuth2TokenValidationServiceHttpsSoap12Endpoint/";
        OAuth2TokenValidationServiceStub stub = new OAuth2TokenValidationServiceStub(targetEndpointUrl);
        oAuth2TokenValidationService = stub;

        ServiceClient client = stub._getServiceClient();
        HttpTransportProperties.Authenticator authenticator = new HttpTransportProperties.Authenticator();
        authenticator.setUsername(adminUserName);
        authenticator.setPassword(adminPassword);
        authenticator.setPreemptiveAuthentication(true);
        Options options = client.getOptions();
        options.setProperty(HTTPConstants.AUTHENTICATE, authenticator);

        client.setOptions(options);
    }

    /**
     * Set trustStore system properties
     * @param trustStorePath javax.net.ssl.trustStore system property
     * @param trustStorePassword javax.net.ssl.trustStorePassword system property
     */
    public void initTrustStore(String trustStorePath, String trustStorePassword) {
        //set trust store properties required in SSL communication.
        System.setProperty("javax.net.ssl.trustStore", trustStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
    }

    public boolean tokenIsValid(String access_token) throws Exception {
        OAuth2TokenValidationRequestDTO req = createRequestTokenDTO(access_token);
        OAuth2TokenValidationResponseDTO resp = oAuth2TokenValidationService.validate(req);
        boolean isValid = resp.getValid();
        return isValid;
    }

    public TokenInfo getTokenInfo(String access_token) throws Exception {
        OAuth2TokenValidationRequestDTO req = createRequestTokenDTO(access_token);

        OAuth2TokenValidationResponseDTO resp = oAuth2TokenValidationService.validate(req);
        String authorizedUser = resp.getAuthorizedUser();
        List<String> scope = Arrays.asList( resp.getScope() );
        Long expiryTime = resp.getExpiryTime();
        String errorMsg = resp.getErrorMsg();

        TokenInfo tinfo = new TokenInfo();
        tinfo.setAuthorizedUser(authorizedUser);
        tinfo.setScope(scope);
        tinfo.setErrorMsg(errorMsg);
        tinfo.setExpiryTime(expiryTime);
        return tinfo;
    }



    private OAuth2TokenValidationRequestDTO createRequestTokenDTO(String access_token) {
        OAuth2TokenValidationRequestDTO req = new OAuth2TokenValidationRequestDTO();
        req.setTokenType("bearer");
        req.setAccessToken(access_token);
        return req;
    }
}
