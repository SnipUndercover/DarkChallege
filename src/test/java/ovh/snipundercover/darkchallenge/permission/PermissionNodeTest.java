package ovh.snipundercover.darkchallenge.permission;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PermissionNodeTest {
	
	@Test
	@DisplayName("Ensuring PermissionNode hierarchy")
	void getSubPermission() {
		String bar = "bar";
		String baz = "baz";
		PermissionNode barNode = PermissionNode.getPermission(bar);
		PermissionNode bazNode = PermissionNode.getPermission(baz);
		PermissionNode bazBarNode = bazNode.getSubPermission(bar);
		PermissionNode barBazNode = barNode.getSubPermission(baz);
		assertEquals("darkchallenge.bar", barNode.getPermissionString());
		assertEquals("darkchallenge.baz", bazNode.getPermissionString());
		assertEquals("darkchallenge.bar.baz", barBazNode.getPermissionString());
		assertEquals("darkchallenge.baz.bar", bazBarNode.getPermissionString());
		assertEquals(barNode, barBazNode.getParent());
		assertEquals(bazNode, bazBarNode.getParent());
	}
}