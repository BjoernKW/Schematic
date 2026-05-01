package com.bjoernkw.schematic;

import com.bjoernkw.schematic.aiup.UseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SchematicApplicationTests {

	@Test
	@UseCase(id = "UC-001")
	void contextLoads() {
	}

}
