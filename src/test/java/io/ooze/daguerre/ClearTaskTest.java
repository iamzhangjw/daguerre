package io.ooze.daguerre;

import io.ooze.daguerre.task.ClearExpiredFileTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * FileService test
 *
 * @author zhangjw
 * @date 2022/05/02 0002 14:57
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DaguerreApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ClearTaskTest {
    @Autowired
    private ClearExpiredFileTask task;


    @Test
    public void testTask() {
        task.run();
    }
}