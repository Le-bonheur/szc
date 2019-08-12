package szc.controller;

import java.util.Base64;
import java.util.concurrent.atomic.AtomicLong;

import szc.entity.Greeting;
import net.sf.json.JSONArray;
import org.apache.commons.codec.Charsets;
import org.springframework.web.bind.annotation.*;
import szc.repository.GreetingRepository;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
public class GreetingController {

    private final GreetingRepository greetingRepository;

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @Autowired
    public GreetingController(GreetingRepository greetingRepository) {
        this.greetingRepository = greetingRepository;
    }

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
        return new Greeting(counter.incrementAndGet(),
                            String.format(template, name));
    }

    @GetMapping("/getPidList")
    public JSONArray getPidList() {
        return greetingRepository.getPidListSP();
    }

    @PostMapping("/insert")
    public void insert(@RequestParam(value="name")String name) {
        String encodeName = Base64.getEncoder().encodeToString(name.getBytes(Charsets.UTF_8));
        System.out.println(encodeName);
        greetingRepository.insert(encodeName);
    }

    @RequestMapping("/tTest")
    public void tTest(@RequestParam(value="name", defaultValue="World") String name) {
        for (int i = 0; i < 5; i++) {
            greetingRepository.transactionTest(i);
            if(i == 3) {
                throw new RuntimeException("Test");
            }
        }
    }
}
