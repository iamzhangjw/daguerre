package pers.zjw.daguerre;

import pers.zjw.daguerre.constant.Algorithm;
import pers.zjw.daguerre.pojo.entity.Credential;
import pers.zjw.daguerre.service.CredentialService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * EndpointTest
 *
 * @author zhangjw
 * @date 2022/11/06 0006 10:10
 */
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DaguerreApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EndpointTest {
    @Resource
    private MockMvc mockMvc;
    @Autowired
    private CredentialService credentialService;

    private Credential credential;

    @Before
    public void setup() {
        credential = credentialService.getByAccessKey("bzThOgtPBJ4cd5");
    }

    @Test
    public void testMockWhenUploadSmallFile() throws Exception {
        Path path = Paths.get("E:\\downloads\\glogg-latest-x86_64-setup.exe");
        String filename = "glogg-latest-x86_64-setup.exe";
        MockMultipartFile file = new MockMultipartFile("file", filename,
                MediaType.APPLICATION_OCTET_STREAM_VALUE, new FileInputStream(path.toFile()));

        Map<String, String> contentTypeParams = new HashMap<>();
        contentTypeParams.put("boundary", ""+file.getSize());
        MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("expireDays", "2");
        mockMvc.perform(MockMvcRequestBuilders.multipart("/oss/u")
                .file(file)
                .contentType(mediaType)
                .params(params)
                .queryParams(baseQueryParams(params))
        ).andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testMockWhenUploadBigFile() throws Exception {
        Path path = Paths.get("E:\\downloads\\WinSCP-5.19.4-Setup.exe");
        String filename = "WinSCP-5.19.4-Setup.exe";
        MockMultipartFile file = new MockMultipartFile("file", filename,
                MediaType.APPLICATION_OCTET_STREAM_VALUE, new FileInputStream(path.toFile()));

        Map<String, String> contentTypeParams = new HashMap<>();
        contentTypeParams.put("boundary", ""+file.getSize());
        MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("expireDays", "2");
        mockMvc.perform(MockMvcRequestBuilders.multipart("/oss/u")
                .file(file)
                .contentType(mediaType)
                .params(params)
                .queryParams(baseQueryParams(params))
        ).andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testMockWhenUploadFileWithoutParams() throws Exception {
        Path path = Paths.get("E:\\downloads\\apache-skywalking-apm-8.8.1-src.tgz");
        String filename = "apache-skywalking-apm-8.8.1-src.tgz";
        MockMultipartFile file = new MockMultipartFile("file", filename,
                MediaType.APPLICATION_OCTET_STREAM_VALUE, new FileInputStream(path.toFile()));

        Map<String, String> contentTypeParams = new HashMap<>();
        contentTypeParams.put("boundary", ""+file.getSize());
        MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/oss/u")
                .file(file)
                .contentType(mediaType)
                .queryParams(baseQueryParams(null))
        ).andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk());
    }

    private MultiValueMap<String, String> baseQueryParams(MultiValueMap<String, String> urlParams) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>(), temp = new LinkedMultiValueMap<>();
        temp.add("accessKey", credential.getAccessKey());
        temp.add("nonce", RandomStringUtils.randomAlphanumeric(5));
        temp.add("timestamp", "" + System.currentTimeMillis()/1000);
        params.addAll(temp);
        if (!CollectionUtils.isEmpty(urlParams)) {
            urlParams.forEach((key, value) -> temp.add(key, value.get(0)));
        }

        StringBuilder source = temp.entrySet().stream()
                .filter(e -> null != e.getValue() && StringUtils.hasText(e.getValue().get(0)))
                .filter(e -> !e.getKey().equals("sign"))
                .sorted(Map.Entry.comparingByKey())
                .collect(StringBuilder::new,
                        (buffer, e) -> buffer.append("&")
                                .append(e.getKey())
                                .append("=")
                                .append(e.getValue().get(0)),
                        StringBuilder::append);
        Algorithm algorithm = Algorithm.MD5;
        Optional<Algorithm> algorithmOp = Algorithm.parse(credential.getAlgorithm());
        if (algorithmOp.isPresent()) {
            algorithm = algorithmOp.get();
        }
        params.add("sign", algorithm.digest(credential.getAccessSecret(), source.toString()));
        return params;
    }
}
