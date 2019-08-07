package hello;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

//    private final GreetingRepository greetingRepository;
    private final Repo repo;

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @Autowired
    public GreetingController(Repo repo) {
        this.repo = repo;
    }

//    @Autowired
//    public GreetingController(GreetingRepository greetingRepository) {
//        this.greetingRepository = greetingRepository;
//    }

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
        return new Greeting(counter.incrementAndGet(),
                            String.format(template, name));
    }

//    @RequestMapping("/test")
//    public List<String> test(@RequestParam(value="name", defaultValue="World") String name) {
//        return greetingRepository.pidList();
//    }

//    @RequestMapping("/otest")
//    public List<String> oracleTest(@RequestParam(value="name", defaultValue="World") String name) {
//        Repo repo = new Repo(jdbcTemplate);
//        List<String> pids = new ArrayList<>();
//        try {
//            pids.addAll(repo.query("SELECT * FROM DEV.BLK_FLG_PID_T", true));
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        repo.close();
//        return pids;
//    }
//
//    @RequestMapping("/oinsert")
//    public void oinsert(@RequestParam(value="name", defaultValue="World") String name) {
//        Repo repo = new Repo(jdbcTemplate);
//        try {
//            repo.query("INSERT INTO DEV.BLK_FLG_PID_T (CBI_CLT_PID) VALUES ('" + name + "')");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        repo.close();
//    }

    @RequestMapping("/tTest")
//    @Transactional(rollbackFor = RuntimeException.class)
    public void tTest(@RequestParam(value="name", defaultValue="World") String name) {
        for (int i = 0; i < 5; i++) {
            repo.transactionTest(i);
            if(i == 3) {
                throw new RuntimeException("Test");
            }
        }
    }
}
