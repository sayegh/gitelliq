package com.gitelliq.gqhc.registration;

import static com.gitelliq.gqhc.Constants.INFO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.gitelliq.gqhc.jersey.EngineLookupException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author sme
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EngineTest {

	private static final int TENMINS = 10 * 60;	//	in seconds

	Date FIXED_DATE_A = new Timestamp(new GregorianCalendar(2012, Calendar.SEPTEMBER,12,13,30,0).getTimeInMillis());
	Date FIXED_DATE_B = new Timestamp(new GregorianCalendar(1972,Calendar.JANUARY,2,7,30,0).getTimeInMillis());
	Date FIXED_DATE_C = new Timestamp(new GregorianCalendar(1916,Calendar.OCTOBER,15,15,30,0).getTimeInMillis());

	private static final Logger LOG = Logger.getLogger("com.gitelliq.gqhc.registration.EngineTest");

	static String prefix = EngineTest.class.getSimpleName() + "_";

	static boolean ALL = false;
	static boolean VERIFIED = true;

	private static Engine copy(Engine engine) {

		Engine copy = create(engine.id, engine.localID, engine.remoteIP, engine.secret);

		copy.setId(engine.getId());

		return copy;
	}

	private static Engine create(String id, String lid, String rip, String secret) {

		Engine engine = new Engine();

		if (id!=null) engine.setId(id.startsWith(prefix) ? id : (prefix + id));
		engine.setRemoteIp(rip);
		if (lid!=null) engine.setlocalId(lid.startsWith(prefix) ? lid : (prefix + lid));
		engine.setSecret(secret);

		return engine;
	}

	private static IpInterface create(Engine engine, String mac, String lip) {

		EngineMac ngnmac = new EngineMac();
		IpInterface ipf = new IpInterface();

		ngnmac.setEngine(engine);
		ngnmac.setMac(mac);
		ipf.setEngineMac(ngnmac);
		ipf.setIp(lip);

		return ipf;
	}

	protected EntityManager getTestEntityManager() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("Test-GQ-Central");
		return emf.createEntityManager();
	}

	@BeforeClass
	public static void setUpBeforeClass() {
	}

	@AfterClass
	public static void tearDownAfterClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	/*
	 * 	This utility seems to be hitting a problem with isNull in Hibernate!
	 * 	Countung IS NOT NULL works, but counting IS NULL doesnt!
	 */

	int countEngines(boolean verified) {
		String qstring = "SELECT e FROM Engine as e";
		if (verified) qstring += " WHERE e.verifiedAt IS NOT NULL";
		TypedQuery<Engine> query = getTestEntityManager().createQuery(qstring, Engine.class);
		return query.getResultList().size();
	}

	int countInterfaces(boolean pending) {
		final String qstring = "SELECT e FROM IpInterface as e";
		TypedQuery<IpInterface> query = getTestEntityManager().createQuery(qstring, IpInterface.class);
		return query.getResultList().size();
	}

	@Test
	public void T00_noRegistrations() {
		final EntityManager entityManager = getTestEntityManager();
		final List<Engine> engines = Engine.findByRemoteIp(entityManager, "192.168.2.111", TENMINS);
		assertEquals(0, engines.size());
	}

	@Test
	public void T10_registerEngine() {
		final Engine engine = create("00-00_uuid", "00-00_local", "87.139.187.77", "Never to be told");
		final EntityManager em = getTestEntityManager();

        em.getTransaction().begin();
        engine.persist(em);
        em.getTransaction().commit();
        em.close();
		assertEquals(1, countEngines(ALL));
		assertEquals(0, countEngines(VERIFIED));
	}

	@Test
	public void T11_getEngineById() {
		final EntityManager em = getTestEntityManager();
		final Engine engine = Engine.findById(em, prefix + "00-00_uuid");

		assertNotNull(engine);
		assertEquals(prefix + "00-00_uuid", engine.getId());
		assertEquals("87.139.187.77", engine.getRemoteIp());
		assertEquals(prefix + "00-00_local", engine.getlocalId());
		assertEquals("Never to be told", engine.getSecret());
		assertTrue(engine.isPending());
		assertEquals(1, countEngines(ALL));
		assertEquals(0, countEngines(VERIFIED));
	}

	/**
	 * 	In the version where pending versions are held in memory, findById
	 * 	will return the same object. Little point in testing that here at
	 * 	the moment. But this test will indicate for us, that this
	 * 	behavior has changed (and that a better test in perhaps needed).
	 *
	 * 	Hahaha - the EntityManager is caching as well - so we can test,
	 * 	will have to do it the old-fashioned way.
	 *
	 * 	@throws NamingException
	 */

	@Test
	public void T12_updateEngine() throws NamingException {

		EntityManager em = getTestEntityManager();
		Engine engine = Engine.findById(em, prefix + "00-00_uuid");
		Engine update = create(engine.id, "00-01_local", "22.222.22.22", "kept under your hat");

		update.setInterfaces(engine.getInterfaces());

        em.getTransaction().begin();
		engine.update(em, update);
        em.getTransaction().commit();

		engine = Engine.findById(em, prefix + "00-00_uuid");

		assertEquals(prefix + "00-00_uuid", engine.getId());
		assertEquals("22.222.22.22", engine.getRemoteIp());
		assertEquals(prefix + "00-01_local", engine.getlocalId());
		assertEquals("kept under your hat", engine.getSecret());
		assertTrue(engine.isPending());
		assertNull(engine.getVerifiedAt());
		assertEquals(1, countEngines(ALL));
		assertEquals(0, countEngines(VERIFIED));
	}

	@Test
	public void T21_persistEngine() throws NamingException {

		EntityManager em = getTestEntityManager();
		Engine engine = Engine.findById(em, prefix + "00-00_uuid");

		assertTrue(engine.isPending());

		engine.setVerifiedAt(FIXED_DATE_A);

        em.getTransaction().begin();
        engine.persist(em);
        em.getTransaction().commit();
        em.close();

		assertEquals(1, countEngines(false));
	}

	@Test
	public void T22_getPersistedEngineById() throws NamingException {

		EntityManager em = getTestEntityManager();
		Engine engine = Engine.findById(em, prefix + "00-00_uuid");

		assertNotNull(engine);
		assertEquals(prefix + "00-00_uuid", engine.getId());
		assertEquals("22.222.22.22", engine.getRemoteIp());
		assertEquals(prefix + "00-01_local", engine.getlocalId());
		assertEquals("kept under your hat", engine.getSecret());
		assertFalse(engine.isPending());
		assertTrue(engine.getVerifiedAt().equals(FIXED_DATE_A));
		assertEquals(1, countEngines(false));
	}

	@Test
	public void T23_updateEngine() throws NamingException {

		EntityManager em = getTestEntityManager();
		Engine engine = Engine.findById(em, prefix + "00-00_uuid");
		Engine update = create(null, "00-02_local", "12.122.12.12", "and dark places");

		update.setId(engine.getId());
		update.setInterfaces(engine.getInterfaces());;

        em.getTransaction().begin();
		engine.update(em, update);
        em.getTransaction().commit();

		engine = Engine.findById(em, prefix + "00-00_uuid");

		assertNotNull(engine);
		assertEquals(prefix + "00-00_uuid", engine.getId());
		assertEquals("12.122.12.12", engine.getRemoteIp());
		assertEquals(prefix + "00-02_local", engine.getlocalId());
		assertEquals("and dark places", engine.getSecret());
		assertFalse(engine.isPending());
		assertTrue(engine.getVerifiedAt().equals(FIXED_DATE_A));
		assertEquals(1, countEngines(false));

        em.close();
	}

	@Test
	public void T30_findByRemoteIp() throws NamingException {

		EntityManager em = getTestEntityManager();
		List<Engine> engines;

		engines = Engine.findByRemoteIp(em, "87.139.187.77", TENMINS);

		assertEquals(0, engines.size());

		engines = Engine.findByRemoteIp(em, "12.122.12.12", TENMINS);

		assertEquals(1, engines.size());

	}

	@Test
	public void T31_moreRemoteIps() throws NamingException {

		EntityManager em = getTestEntityManager();
		Engine engine = null;

		engine = create("01-00_uuid", "01-00_local", "87.139.187.77", "Never to be told");

        em.getTransaction().begin();
		engine.persist(em);
//        em.getTransaction().commit();

//        assertEquals(2, countEngines(false));
//        assertEquals(1, countEngines(true));       // TODO: FIX

        engine = create("02-00_uuid", "02-00_local", "22.222.22.22", "Never to be told");

//        em.getTransaction().begin();
		engine.persist(em);
        em.getTransaction().commit();

		assertEquals(3, countEngines(ALL));
		assertEquals(1, countEngines(VERIFIED));
//
//
//        assertEquals(3, countEngines(false));      //	Now 3 engines all together, only 1 pending still ...
//        assertEquals(1, countEngines(true));       // TODO: FIX
	}

	@Test
	public void T32_findByRemoteIp() throws NamingException {

		EntityManager em = getTestEntityManager();
		List<Engine> engines = null;

		assertEquals(1, (engines=Engine.findByRemoteIp(em, "87.139.187.77", TENMINS)).size());
		assertEquals(1, (engines=Engine.findByRemoteIp(em, "12.122.12.12", TENMINS)).size());
		assertEquals(1, (engines=Engine.findByRemoteIp(em, "22.222.22.22", TENMINS)).size());
		assertEquals(0, (engines=Engine.findByRemoteIp(em, "0.0.0.0", TENMINS)).size());
	}

	@Test
	public void T33_persistEngine() throws NamingException {

		EntityManager em = getTestEntityManager();
		Engine engine = create("01-00_uuid", null, null, null);

		engine = Engine.findById(em, engine.getId());

        em.getTransaction().begin();
		engine.setVerifiedAt(FIXED_DATE_B);
		engine.update(em,copy(engine));
        em.getTransaction().commit();

        assertEquals(2, countEngines(true));	// TODO: FIX
        assertEquals(3, countEngines(false));

        T32_findByRemoteIp();
	}

	@Test
	public void T40_duplicateRemoteIp() throws NamingException {

		EntityManager em = getTestEntityManager();
		Engine engine = create("03-00_uuid", "03-00_local", "87.139.187.77", "secret things");

        em.getTransaction().begin();
		engine.persist(em);
        em.getTransaction().commit();

        assertEquals(4, countEngines(ALL));
        assertEquals(2, countEngines(VERIFIED));
		assertEquals(2, Engine.findByRemoteIp(em, "87.139.187.77", TENMINS).size());
	}

	@Test
	public void T41_addInterfaces() throws NamingException {

		EntityManager em = getTestEntityManager();
		Engine update = null;
		Engine engine = null;
		IpInterface ipf = null;
		List<IpInterface> ipfList = null;

		engine = Engine.findById(em, create("03-00_uuid",null,null,null).getId());
		assertEquals("87.139.187.77",engine.getRemoteIp());
		assertTrue(engine.isPending());

		ipfList = new ArrayList<IpInterface>();
		update = copy(engine);

		ipfList.add(create(engine,"00:00:00:00:00:00:00:00","192.168.1.1"));
		update.setInterfaces(ipfList);

        em.getTransaction().begin();
		engine.update(em, update);
        em.getTransaction().commit();

        assertEquals(4, countEngines(ALL));
        assertEquals(2, countEngines(VERIFIED));
        assertEquals(1, countInterfaces(false));

		engine = Engine.findById(em, create("01-00_uuid",null,null,null).getId());
		assertEquals("87.139.187.77",engine.getRemoteIp());
		assertFalse(engine.isPending());
		assertTrue(engine.getVerifiedAt().equals(FIXED_DATE_B));

		ipfList = new ArrayList<IpInterface>();
		update = copy(engine);

		ipfList.add(create(engine,"11:11:11:11:11:11:11:11","192.168.1.1"));
		ipfList.add(create(engine,"22:22:22:22:22:22:22:22","10.2.100.22"));
		update.setInterfaces(ipfList);

        em.getTransaction().begin();
		engine.update(em, update);
        em.getTransaction().commit();

        assertEquals(4, countEngines(ALL));
        assertEquals(2, countEngines(VERIFIED));
        assertEquals(3, countInterfaces(false));
	}

	@Test
	public void T42_findByRemoteIp() throws NamingException {

		EntityManager em = getTestEntityManager();
		List<Engine> engines = null;
		List<String> macs = new ArrayList<String>();

		macs.add("11:11:11:11:11:11:11:11");
		macs.add("00:00:00:00:00:00:00:00");
		macs.add("22:22:22:22:22:22:22:22");

		engines = Engine.findByRemoteIp(em, "87.139.187.77", TENMINS);

		assertEquals(2, engines.size());
		for (Engine engine : engines)
			for (IpInterface ifp : engine.getInterfaces())
				macs.remove(ifp.getMac());

		assertTrue(macs.isEmpty());

		engines = Engine.findByRemoteIp(em, "12.122.12.12", TENMINS);

		assertEquals(1, engines.size());
		assertTrue(engines.get(0).getInterfaces().isEmpty());

		engines = Engine.findByRemoteIp(em, "22.222.22.22", TENMINS);

		assertEquals(1, engines.size());
		assertTrue(engines.get(0).getInterfaces().isEmpty());

		assertEquals(0, Engine.findByRemoteIp(em, "0.0.0.0", TENMINS).size());
	}

	@Test
	public void T43_getById() throws NamingException {

		EntityManager em = getTestEntityManager();
		Engine engine = null;

		assertNull(engine=Engine.findById(em, prefix + "bogus_uuid"));
		assertNotNull(engine=Engine.findById(em, prefix + "00-00_uuid"));
		assertTrue(engine.getInterfaces().isEmpty());
		assertNotNull(engine=Engine.findById(em, prefix + "01-00_uuid"));
		assertEquals(2, engine.getInterfaces().size());
		assertNotNull(engine=Engine.findById(em, prefix + "02-00_uuid"));
		assertTrue(engine.getInterfaces().isEmpty());
		assertNotNull(engine=Engine.findById(em, prefix + "03-00_uuid"));
		assertEquals(1, engine.getInterfaces().size());
	}

	@Test
	public void T44_getByMac() throws NamingException, EngineLookupException {

		EntityManager em = getTestEntityManager();
		Engine engine = null;
		List<String> macs;
		Exception e = null;

		macs = Arrays.asList(new String[] {"00:00:00:00:00:00:00:00"});
		engine = Engine.findByMacAndRemoteIp(em, "87.139.187.77", macs, TENMINS);
		assertEquals(prefix + "03-00_uuid", engine.getId());
		assertNull(Engine.findByMacAndRemoteIp(em, "12.122.12.12", macs, TENMINS));
		assertNull(Engine.findByMacAndRemoteIp(em, "22.222.22.22", macs, TENMINS));

		macs = Arrays.asList(new String[] {"11:11:11:11:11:11:11:11"});
		engine = Engine.findByMacAndRemoteIp(em, "87.139.187.77", macs, TENMINS);
		assertNotNull(engine);
		assertEquals(prefix + "01-00_uuid", engine.getId());
		assertNull(Engine.findByMacAndRemoteIp(em, "12.122.12.12", macs, TENMINS));
		assertNull(Engine.findByMacAndRemoteIp(em, "22.222.22.22", macs, TENMINS));

		macs = Arrays.asList(new String[] {"22:22:22:22:22:22:22:22"});
		engine = Engine.findByMacAndRemoteIp(em, "87.139.187.77", macs, TENMINS);
		assertNotNull(engine);
		assertEquals(prefix + "01-00_uuid", engine.getId());
		assertNull(Engine.findByMacAndRemoteIp(em, "12.122.12.12", macs, TENMINS));
		assertNull(Engine.findByMacAndRemoteIp(em, "22.222.22.22", macs, TENMINS));

		macs = Arrays.asList(new String[] {});
		assertNull(Engine.findByMacAndRemoteIp(em, "87.139.187.77", macs, TENMINS));

		macs = Arrays.asList(new String[] {"99:99:99:99:99:99:99:99"});
		assertNull(Engine.findByMacAndRemoteIp(em, "87.139.187.77", macs, TENMINS));
	}

	@Test
	public void T44_getNewByMac() throws NamingException, EngineLookupException, InterruptedException {

		EntityManager em = getTestEntityManager();
		Engine engine = null;
		List<String> macs;
		Exception e = null;

		Thread.sleep(5*1000);

		macs = Arrays.asList(new String[] {"11:11:11:11:11:11:11:11"});
		engine = Engine.findByMacAndRemoteIp(em, "87.139.187.77", macs, 5);
		assertNull(engine);
	}

	public void ST50_changeInterface(boolean pending, int all, int verified, int interfaces)
			throws NamingException, EngineLookupException
	{
		EntityManager em = getTestEntityManager();
		Engine update = null;
		Engine engine = null;
		IpInterface ipf = null;
		List<IpInterface> ipfList = null;

		engine = Engine.findById(em, create("03-00_uuid",null,null,null).getId());
		assertEquals("87.139.187.77",engine.getRemoteIp());
		assertEquals(pending, engine.isPending());

		ipfList = new ArrayList<IpInterface>();
		update = copy(engine);

		ipfList.add(create(engine,"33:33:33:33:33:33:33:33","200.11.22.33"));
		update.setInterfaces(ipfList);

        em.getTransaction().begin();
		engine.update(em, update);
        em.getTransaction().commit();

        assertEquals(all, countEngines(ALL));
        assertEquals(verified, countEngines(VERIFIED));
        assertEquals(interfaces, countInterfaces(false));
	}

	public void ST51_getByMac(boolean pending)
			throws NamingException, EngineLookupException
	{
		EntityManager em = getTestEntityManager();
		Engine engine = null;
		List<String> macs;
		Exception e = null;

		macs = Arrays.asList(new String[] {"00:00:00:00:00:00:00:00"});
		assertNull(Engine.findByMacAndRemoteIp(em, "87.139.187.77", macs, TENMINS));

		macs = Arrays.asList(new String[] {"33:33:33:33:33:33:33:33"});
		engine = Engine.findByMacAndRemoteIp(em, "87.139.187.77", macs, TENMINS);
		assertNotNull(engine);
		assertEquals(pending, engine.isPending());
		assertEquals(prefix + "03-00_uuid", engine.getId());
		assertNull(Engine.findByMacAndRemoteIp(em, "12.122.12.12", macs, TENMINS));
		assertNull(Engine.findByMacAndRemoteIp(em, "22.222.22.22", macs, TENMINS));
	}

	public void ST52_addInterface(boolean pending, int all, int verified, int interfaces)
			throws NamingException, EngineLookupException
	{
		EntityManager em = getTestEntityManager();
		Engine update = null;
		Engine engine = null;
		IpInterface ipf = null;
		List<IpInterface> ipfList = null;

		engine = Engine.findById(em, create("03-00_uuid",null,null,null).getId());
		assertEquals("87.139.187.77",engine.getRemoteIp());
		assertEquals(pending, engine.isPending());

		ipfList = new ArrayList<IpInterface>();
		update = copy(engine);

		ipfList.add(create(engine,"33:33:33:33:33:33:33:33","200.11.22.33"));
		ipfList.add(create(engine,"44:44:44:44:44:44:44:44","100.10.100.10"));
		update.setInterfaces(ipfList);

        em.getTransaction().begin();
		engine.update(em, update);
        em.getTransaction().commit();

        assertEquals(all, countEngines(ALL));
        assertEquals(verified, countEngines(VERIFIED));
        assertEquals(interfaces, countInterfaces(false));
	}

	public void ST53_getByMac(boolean pending)
			throws NamingException, EngineLookupException
	{
		EntityManager em = getTestEntityManager();
		Engine engine = null;
		List<String> macs;
		Exception e = null;

		macs = Arrays.asList(new String[] {"00:00:00:00:00:00:00:00"});
		assertNull(Engine.findByMacAndRemoteIp(em, "87.139.187.77", macs, TENMINS));

		macs = Arrays.asList(new String[] {"33:33:33:33:33:33:33:33"});
		engine = Engine.findByMacAndRemoteIp(em, "87.139.187.77", macs, TENMINS);
		assertNotNull(engine);
		assertEquals(pending, engine.isPending());
		assertEquals(prefix + "03-00_uuid", engine.getId());
		assertNull(Engine.findByMacAndRemoteIp(em, "12.122.12.12", macs, TENMINS));
		assertNull(Engine.findByMacAndRemoteIp(em, "22.222.22.22", macs, TENMINS));

		macs = Arrays.asList(new String[] {"44:44:44:44:44:44:44:44"});
		engine = Engine.findByMacAndRemoteIp(em, "87.139.187.77", macs, TENMINS);
		assertNotNull(engine);
		assertEquals(pending, engine.isPending());
		assertEquals(prefix + "03-00_uuid", engine.getId());
		assertNull(Engine.findByMacAndRemoteIp(em, "12.122.12.12", macs, TENMINS));
		assertNull(Engine.findByMacAndRemoteIp(em, "22.222.22.22", macs, TENMINS));
	}

	public void ST54_removeInterface(boolean pending, int all, int verified, int interfaces)
			throws NamingException, EngineLookupException
	{
		EntityManager em = getTestEntityManager();
		Engine update = null;
		Engine engine = null;
		IpInterface ipf = null;
		List<IpInterface> ipfList = null;

		engine = Engine.findById(em, create("03-00_uuid",null,null,null).getId());
		assertEquals("87.139.187.77",engine.getRemoteIp());
		assertEquals(pending, engine.isPending());

		ipfList = new ArrayList<IpInterface>();
		update = copy(engine);

		ipfList.add(create(engine,"44:44:44:44:44:44:44:44","100.10.100.10"));
		update.setInterfaces(ipfList);

        em.getTransaction().begin();
		engine.update(em, update);
        em.getTransaction().commit();

        assertEquals(all, countEngines(ALL));
        assertEquals(verified, countEngines(VERIFIED));
        assertEquals(interfaces, countInterfaces(false));
	}

	public void ST55_getByMac(boolean pending)
			throws NamingException, EngineLookupException
	{
		EntityManager em = getTestEntityManager();
		Engine engine = null;
		List<String> macs;
		Exception e = null;

		macs = Arrays.asList(new String[] {"00:00:00:00:00:00:00:00"});
		assertNull(Engine.findByMacAndRemoteIp(em, "87.139.187.77", macs, TENMINS));

		macs = Arrays.asList(new String[] {"33:33:33:33:33:33:33:33"});
		assertNull(Engine.findByMacAndRemoteIp(em, "87.139.187.77", macs, TENMINS));

		macs = Arrays.asList(new String[] {"44:44:44:44:44:44:44:44"});
		engine = Engine.findByMacAndRemoteIp(em, "87.139.187.77", macs, TENMINS);
		assertNotNull(engine);
		assertEquals(pending, engine.isPending());
		assertEquals(prefix + "03-00_uuid", engine.getId());
		assertNull(Engine.findByMacAndRemoteIp(em, "12.122.12.12", macs, TENMINS));
		assertNull(Engine.findByMacAndRemoteIp(em, "22.222.22.22", macs, TENMINS));
	}

	@Test
	public void T50_changeInterface() throws NamingException, EngineLookupException {

		ST50_changeInterface(true, 4, 2, 3);
	}

	@Test
	public void T51_getByMac() throws NamingException, EngineLookupException {

		ST51_getByMac(true);
	}

	@Test
	public void T52_addInterface() throws NamingException, EngineLookupException {

		ST52_addInterface(true, 4, 2, 4);
	}

	@Test
	public void T53_getByMac() throws NamingException, EngineLookupException {

		ST53_getByMac(true);
	}

	@Test
	public void T54_removeInterface() throws NamingException, EngineLookupException {

		 ST54_removeInterface(true, 4, 2, 3);
	}

	@Test
	public void T55_getByMac() throws NamingException, EngineLookupException {

		ST55_getByMac(true);
	}

	public void T56_revert() throws NamingException, EngineLookupException {

		EntityManager em = getTestEntityManager();
		Engine update = null;
		Engine engine = null;
		IpInterface ipf = null;
		List<IpInterface> ipfList = null;

		engine = Engine.findById(em, create("03-00_uuid",null,null,null).getId());
		assertEquals("87.139.187.77",engine.getRemoteIp());
		assertTrue(engine.isPending());

		ipfList = new ArrayList<IpInterface>();
		update = copy(engine);

		ipfList.add(create(engine,"00:00:00:00:00:00:00:00","192.168.1.1"));
		update.setInterfaces(ipfList);

        em.getTransaction().begin();
		engine.update(em, update);
        em.getTransaction().commit();

        assertEquals(2, countEngines(false));
        assertEquals(2, countInterfaces(false));
	}

	@Test
	public void T57_persist() throws NamingException, EngineLookupException {

		EntityManager em = getTestEntityManager();
		Engine engine = null;

		engine = Engine.findById(em, create("03-00_uuid",null,null,null).getId());
		assertEquals("87.139.187.77",engine.getRemoteIp());
		assertTrue(engine.isPending());

        em.getTransaction().begin();
		engine.setVerifiedAt(FIXED_DATE_C);
		engine.persist(em);
        em.getTransaction().commit();

        assertEquals(4, countEngines(ALL));
        assertEquals(3, countEngines(VERIFIED));
        assertEquals(3, countInterfaces(false));
	}

	@Test
	public void T60_changeInterface() throws NamingException, EngineLookupException {

		ST50_changeInterface(false, 4, 3, 3);
	}

	@Test
	public void T61_getByMac() throws NamingException, EngineLookupException {

		ST51_getByMac(false);
	}

	@Test
	public void T62_addInterface() throws NamingException, EngineLookupException {

		ST52_addInterface(false, 4, 3, 4);
	}

	@Test
	public void T63_getByMac() throws NamingException, EngineLookupException {

		ST53_getByMac(false);
	}

	@Test
	public void T64_removeInterface() throws NamingException, EngineLookupException {

		 ST54_removeInterface(false, 4, 3, 3);
	}

	@Test
	public void T65_getByMac() throws NamingException, EngineLookupException {

		ST55_getByMac(false);
	}

	@Test
	public void T70_addPending() throws NamingException {

		LOG.log(INFO, "T70_addPending()");

		EntityManager em = getTestEntityManager();

        em.getTransaction().begin();
        Engine.limitPending(em, 2);
		create("70-00_uuid", "70-00_local", "84.100.100.100", "secret").persist(em);
        em.getTransaction().commit();

        assertEquals(5, countEngines(ALL));
        assertEquals(3, countEngines(VERIFIED));
	}

	@Test
	public void T71_addPending() throws NamingException {

		LOG.log(INFO, "T71_addPending()");

		EntityManager em = getTestEntityManager();

        em.getTransaction().begin();
        Engine.limitPending(em, 2);
		create("71-00_uuid", "71-00_local", "85.100.100.100", "secret").persist(em);
        em.getTransaction().commit();

        assertEquals(6, countEngines(ALL));
        assertEquals(3, countEngines(VERIFIED));
	}

	@Test
	public void T72_addPending() throws NamingException {

		LOG.log(INFO, "T72_addPending()");

		EntityManager em = getTestEntityManager();

        em.getTransaction().begin();
        Engine.limitPending(em, 2);
		create("72-00_uuid", "72-00_local", "85.100.100.100", "secret").persist(em);
        em.getTransaction().commit();

        assertEquals(6, countEngines(ALL));
        assertEquals(3, countEngines(VERIFIED));
	}

	@Test
	public void T73_addPending() throws NamingException {

		LOG.log(INFO, "T73_addPending()");

		EntityManager em = getTestEntityManager();

        em.getTransaction().begin();
        Engine.limitPending(em, 2);
		create("73-00_uuid", "73-00_local", "85.100.100.100", "secret").persist(em);
        em.getTransaction().commit();

        assertEquals(6, countEngines(ALL));
        assertEquals(3, countEngines(VERIFIED));
	}

	public void demo() throws NamingException, EngineLookupException {

		EntityManager em = getTestEntityManager();
		Engine update = null;
		Engine engine = null;
		IpInterface ipf = null;
		List<IpInterface> ipfList = null;

		engine = Engine.findById(em, create("03-00_uuid",null,null,null).getId());
		assertEquals("87.139.187.77",engine.getRemoteIp());
		assertTrue(!engine.isPending());

//		ipfList = new ArrayList<IpInterface>();
		update = copy(engine);
		update.setId("demo");

//		ipfList.add(create(engine,"00:00:00:00:00:00:00:00","192.168.1.1"));
//		update.setInterfaces(ipfList);
//
        em.getTransaction().begin();
		engine.update(em, update);
        em.getTransaction().commit();
//
//        assertEquals(2, countEngines(false));
//        assertEquals(2, countInterfaces(false));
	}

	public void getDemo() throws NamingException, EngineLookupException {

		EntityManager em = getTestEntityManager();
		Engine update = null;
		Engine engine = null;
		IpInterface ipf = null;
		List<IpInterface> ipfList = null;

		engine = Engine.findById(em, "demo");

		assertEquals("87.139.187.77",engine.getRemoteIp());
		assertTrue(!engine.isPending());
	}
}
