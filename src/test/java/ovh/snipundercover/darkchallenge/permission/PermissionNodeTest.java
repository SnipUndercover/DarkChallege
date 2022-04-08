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
		PermissionNode barNode = new PermissionNode(bar);
		PermissionNode bazNode = new PermissionNode(baz);
		PermissionNode bazBarNode = bazNode.getSubPermission(bar);
		PermissionNode barBazNode = barNode.getSubPermission(baz);
		assertEquals("bar", barNode.getPermissionString());
		assertEquals("baz", bazNode.getPermissionString());
		assertEquals("bar.baz", barBazNode.getPermissionString());
		assertEquals("baz.bar", bazBarNode.getPermissionString());
		assertEquals(barNode, barBazNode.getParent());
		assertEquals(bazNode, bazBarNode.getParent());
	}
}