import com.anonymous.mapper.SeatMapper;
import com.anonymous.model.Seat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(classes = com.anonymous.Application.class)
public class SeatMapperTest {
    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private ApplicationContext context;

//    @Test
//    void checkBeans() {
//        String[] names = context.getBeanDefinitionNames();
//        for (String name : names) {
//            if (name.contains("Mapper")) System.out.println("已加载 Bean: " + name);
//        }
//    }

    @Test
    void testDbConnection() {
        Seat seat = seatMapper.selectById(1L);
        if (seat != null) {
            System.out.println("连接成功！座位坐标为: (" + seat.getRowIndex() + "," + seat.getColIndex() + ")");
        } else {
            System.out.println("未找到数据，但连接已建立。");
        }
    }
}
