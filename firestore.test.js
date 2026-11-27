const {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails,
} = require("@firebase/rules-unit-testing");
const { describe, it, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_101";
const BOB_UID = "bob_uid_202";

let testEnv;

function validUserProfile(uid, now = new Date()) {
  return {
    userId: uid,
    displayName: "Rahim Uddin",
    email: "rahim@example.com",
    deviceId: "android_device_01",
    syncRevision: 1,
    createdAt: now,
    updatedAt: now,
  };
}

function validNotebook(uid, now = new Date()) {
  return {
    userId: uid,
    localId: 1,
    name: "ব্যক্তিগত খাতা",
    type: "PERSONAL",
    iconKey: "wallet",
    openingBalancePoisha: 500000,
    archived: false,
    clientCreatedAtMs: 1700000000000,
    clientUpdatedAtMs: 1700000001000,
    createdAt: now,
    updatedAt: now,
  };
}

function validCategory(uid, now = new Date()) {
  return {
    userId: uid,
    localId: 10,
    notebookId: 1,
    nameBn: "বাজার",
    nameEn: "Groceries",
    type: "EXPENSE",
    iconKey: "shopping_cart",
    colorHex: "#176B45",
    defaultCategory: true,
    active: true,
    clientUpdatedAtMs: 1700000001000,
    createdAt: now,
    updatedAt: now,
  };
}

function validShop(uid, now = new Date()) {
  return {
    userId: uid,
    localId: 2,
    notebookId: 1,
    name: "করিম স্টোর",
    ownerName: "আব্দুল করিম",
    phone: "01711000000",
    address: "ধানমন্ডি, ঢাকা",
    shopCategory: "মুদি দোকান",
    openingBalancePoisha: 25000,
    notes: "মাসিক বাকি",
    archived: false,
    clientCreatedAtMs: 1700000000000,
    clientUpdatedAtMs: 1700000001000,
    createdAt: now,
    updatedAt: now,
  };
}

function validPersonalLoan(uid, now = new Date()) {
  return {
    userId: uid,
    localId: 3,
    notebookId: 1,
    direction: "GIVEN",
    personName: "কামাল হোসেন",
    phone: "01811000000",
    principalPoisha: 150000,
    loanDate: 1700000000000,
    dueDate: 1702592000000,
    description: "জরুরি প্রয়োজন",
    settled: false,
    clientCreatedAtMs: 1700000000000,
    clientUpdatedAtMs: 1700000001000,
    createdAt: now,
    updatedAt: now,
  };
}

function validTransaction(uid, now = new Date()) {
  return {
    userId: uid,
    localId: 4,
    notebookId: 1,
    type: "SHOP_PURCHASE",
    totalAmountPoisha: 120000,
    paidAmountPoisha: 50000,
    dueAmountPoisha: 70000,
    categoryId: 10,
    shopId: 2,
    paymentMethod: "CASH",
    note: "চাল ও ডাল",
    transactionDate: 1700000000000,
    deleted: false,
    items: [
      {
        localId: 1,
        productName: "মিনিকেট চাল",
        quantityMillis: 5000,
        unit: "kg",
        unitPricePoisha: 7200,
        lineTotalPoisha: 36000,
      },
    ],
    clientCreatedAtMs: 1700000000000,
    clientUpdatedAtMs: 1700000001000,
    createdAt: now,
    updatedAt: now,
  };
}

function validBudget(uid, now = new Date()) {
  return {
    userId: uid,
    localId: 5,
    notebookId: 1,
    yearMonth: "2026-11",
    categoryId: 10,
    limitPoisha: 800000,
    clientUpdatedAtMs: 1700000001000,
    createdAt: now,
    updatedAt: now,
  };
}

function validNote(uid, now = new Date()) {
  return {
    userId: uid,
    localId: 6,
    notebookId: 1,
    title: "বাজারের তালিকা",
    content: "সকালের বাজার",
    category: "SHOPPING_LIST",
    checklistItems: [
      { text: "তেল ২ লিটার", checked: false },
      { text: "ডিম ১ ডজন", checked: true },
    ],
    contactPhone: "01711000000",
    contactAddress: "মিরপুর",
    pinned: true,
    clientCreatedAtMs: 1700000000000,
    clientUpdatedAtMs: 1700000001000,
    createdAt: now,
    updatedAt: now,
  };
}

function validTask(uid, now = new Date()) {
  return {
    userId: uid,
    localId: 7,
    notebookId: 1,
    title: "বিদ্যুৎ বিল পরিশোধ",
    description: "মাসের ৫ তারিখের আগে",
    taskType: "BILL",
    dueAt: 1702592000000,
    reminderAt: 1702500000000,
    completed: false,
    linkedLoanId: 3,
    clientCreatedAtMs: 1700000000000,
    clientUpdatedAtMs: 1700000001000,
    createdAt: now,
    updatedAt: now,
  };
}

describe("JomaKhoroch Firestore Security Rules", () => {
  before(async () => {
    testEnv = await initializeTestEnvironment({
      projectId: PROJECT_ID,
      firestore: {
        rules: fs.readFileSync("firestore.rules", "utf8"),
        host: "127.0.0.1",
        port: 8085,
      },
    });
  });

  after(async () => {
    if (testEnv) {
      await testEnv.cleanup();
    }
  });

  beforeEach(async () => {
    await testEnv.clearFirestore();
  });

  it("denies unauthenticated access to all user documents and subcollections", async () => {
    const anonDb = testEnv.unauthenticatedContext().firestore();
    await assertFails(anonDb.collection("users").doc(ALICE_UID).get());
    await assertFails(anonDb.collection("users").doc(ALICE_UID).set(validUserProfile(ALICE_UID)));
    await assertFails(anonDb.collection("users").doc(ALICE_UID).collection("notebooks").get());
    await assertFails(anonDb.collection("users").doc(ALICE_UID).collection("transactions").get());
    await assertFails(anonDb.collection("unknown_collection").doc("doc1").get());
  });

  it("denies listing /users collection even when authenticated", async () => {
    const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
    await assertFails(aliceDb.collection("users").get());
  });

  it("enforces user profile CRUD, immutable keys, and cross-user isolation", async () => {
    const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
    const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
    const ref = aliceDb.collection("users").doc(ALICE_UID);

    await assertSucceeds(ref.set(validUserProfile(ALICE_UID)));
    await assertSucceeds(ref.get());

    // Bob cannot read, update, or delete Alice's profile
    await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
    await assertFails(
      bobDb.collection("users").doc(ALICE_UID).update({ displayName: "Hacker", updatedAt: new Date() })
    );
    await assertFails(bobDb.collection("users").doc(ALICE_UID).delete());

    // Valid partial update (mirrors CloudUserProfile.toUpdateMap)
    await assertSucceeds(
      ref.update({
        displayName: "Rahim Updated",
        email: "rahim@example.com",
        deviceId: "android_device_02",
        syncRevision: 2,
        updatedAt: new Date(),
      })
    );

    // Reject immutable userId / createdAt change and ghost field injection
    await assertFails(ref.update({ userId: BOB_UID, updatedAt: new Date() }));
    await assertFails(ref.update({ createdAt: new Date(), updatedAt: new Date() }));
    await assertFails(ref.update({ role: "admin", updatedAt: new Date() }));

    // Reject future timestamp
    const future = new Date(Date.now() + 60000);
    await assertFails(
      bobDb.collection("users").doc(BOB_UID).set(validUserProfile(BOB_UID, future))
    );
  });

  it("validates notebooks create, partial update, list, delete, and strict schema", async () => {
    const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
    const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
    const col = aliceDb.collection("users").doc(ALICE_UID).collection("notebooks");
    const docRef = col.doc("nb_1");

    await assertSucceeds(docRef.set(validNotebook(ALICE_UID)));
    await assertSucceeds(col.get());
    await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("notebooks").get());

    // Partial update matching CloudNotebook.toUpdateMap
    await assertSucceeds(
      docRef.update({
        name: "পারিবারিক খাতা",
        type: "FAMILY",
        iconKey: "home",
        openingBalancePoisha: 750000,
        archived: false,
        clientCreatedAtMs: 1700000000000,
        clientUpdatedAtMs: 1700000005000,
        updatedAt: new Date(),
      })
    );

    // Reject immutable localId change, negative balance, invalid enum, and injected "id"
    await assertFails(docRef.update({ localId: 99, updatedAt: new Date() }));
    await assertFails(docRef.update({ openingBalancePoisha: -10, updatedAt: new Date() }));
    await assertFails(docRef.update({ type: "INVALID_TYPE", updatedAt: new Date() }));
    await assertFails(col.doc("nb_2").set({ ...validNotebook(ALICE_UID), id: "nb_2" }));

    await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("notebooks").doc("nb_1").delete());
    await assertSucceeds(docRef.delete());
  });

  it("validates categories, shops, and personal_loans CRUD and constraints", async () => {
    const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
    const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
    const userDoc = aliceDb.collection("users").doc(ALICE_UID);

    // Categories
    const catRef = userDoc.collection("categories").doc("cat_10");
    await assertSucceeds(catRef.set(validCategory(ALICE_UID)));
    await assertSucceeds(userDoc.collection("categories").get());
    await assertSucceeds(
      catRef.update({
        notebookId: 1,
        nameBn: "মুদি বাজার",
        nameEn: "Grocery",
        type: "EXPENSE",
        iconKey: "shopping_cart",
        colorHex: "#176B45",
        defaultCategory: true,
        active: true,
        clientUpdatedAtMs: 1700000002000,
        updatedAt: new Date(),
      })
    );
    await assertFails(catRef.update({ type: "TRANSFER", updatedAt: new Date() }));
    await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("categories").doc("cat_10").get());

    // Shops
    const shopRef = userDoc.collection("shops").doc("shop_2");
    await assertSucceeds(shopRef.set(validShop(ALICE_UID)));
    await assertSucceeds(userDoc.collection("shops").get());
    await assertSucceeds(
      shopRef.update({
        notebookId: 1,
        name: "করিম জেনারেল স্টোর",
        ownerName: "আব্দুল করিম",
        phone: "01711000000",
        address: "ধানমন্ডি",
        shopCategory: "মুদি",
        openingBalancePoisha: 30000,
        notes: "",
        archived: false,
        clientCreatedAtMs: 1700000000000,
        clientUpdatedAtMs: 1700000003000,
        updatedAt: new Date(),
      })
    );
    await assertFails(shopRef.update({ openingBalancePoisha: -5, updatedAt: new Date() }));

    // Personal loans
    const loanRef = userDoc.collection("personal_loans").doc("loan_3");
    await assertSucceeds(loanRef.set(validPersonalLoan(ALICE_UID)));
    await assertSucceeds(userDoc.collection("personal_loans").get());
    await assertSucceeds(
      loanRef.update({
        notebookId: 1,
        direction: "GIVEN",
        personName: "কামাল হোসেন",
        phone: "01811000000",
        principalPoisha: 150000,
        loanDate: 1700000000000,
        description: "আংশিক পরিশোধিত",
        settled: true,
        clientCreatedAtMs: 1700000000000,
        clientUpdatedAtMs: 1700000004000,
        updatedAt: new Date(),
      })
    );
    await assertFails(loanRef.update({ direction: "UNKNOWN", updatedAt: new Date() }));
  });

  it("validates transactions with native items array, budgets, notes, and tasks", async () => {
    const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
    const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
    const userDoc = aliceDb.collection("users").doc(ALICE_UID);

    // Transactions
    const txRef = userDoc.collection("transactions").doc("tx_4");
    await assertSucceeds(txRef.set(validTransaction(ALICE_UID)));
    await assertSucceeds(userDoc.collection("transactions").get());
    await assertSucceeds(
      txRef.update({
        notebookId: 1,
        type: "SHOP_PURCHASE",
        totalAmountPoisha: 120000,
        paidAmountPoisha: 120000,
        dueAmountPoisha: 0,
        categoryId: 10,
        shopId: 2,
        paymentMethod: "MOBILE_BANKING",
        note: "সম্পূর্ণ পরিশোধ",
        transactionDate: 1700000000000,
        deleted: false,
        clientCreatedAtMs: 1700000000000,
        clientUpdatedAtMs: 1700000005000,
        updatedAt: new Date(),
      })
    );
    await assertFails(txRef.update({ type: "CRYPTO_SWAP", updatedAt: new Date() }));
    await assertFails(txRef.update({ totalAmountPoisha: -100, updatedAt: new Date() }));
    await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("transactions").get());

    // Budgets
    const budgetRef = userDoc.collection("budgets").doc("budget_5");
    await assertSucceeds(budgetRef.set(validBudget(ALICE_UID)));
    await assertSucceeds(userDoc.collection("budgets").get());
    await assertSucceeds(
      budgetRef.update({
        notebookId: 1,
        yearMonth: "2026-12",
        categoryId: 10,
        limitPoisha: 900000,
        clientUpdatedAtMs: 1700000006000,
        updatedAt: new Date(),
      })
    );
    await assertFails(budgetRef.update({ yearMonth: "2026/12", updatedAt: new Date() }));

    // Notes with native checklistItems
    const noteRef = userDoc.collection("notes").doc("note_6");
    await assertSucceeds(noteRef.set(validNote(ALICE_UID)));
    await assertSucceeds(userDoc.collection("notes").get());
    await assertSucceeds(
      noteRef.update({
        notebookId: 1,
        title: "বাজারের তালিকা (আপডেট)",
        content: "সকালের বাজার",
        category: "SHOPPING_LIST",
        checklistItems: [{ text: "তেল ২ লিটার", checked: true }],
        contactPhone: "01711000000",
        contactAddress: "মিরপুর",
        pinned: false,
        clientCreatedAtMs: 1700000000000,
        clientUpdatedAtMs: 1700000007000,
        updatedAt: new Date(),
      })
    );
    await assertFails(noteRef.update({ category: "SECRET_HACK", updatedAt: new Date() }));

    // Tasks
    const taskRef = userDoc.collection("tasks").doc("task_7");
    await assertSucceeds(taskRef.set(validTask(ALICE_UID)));
    await assertSucceeds(userDoc.collection("tasks").get());
    await assertSucceeds(
      taskRef.update({
        notebookId: 1,
        title: "বিদ্যুৎ বিল পরিশোধ",
        description: "পরিশোধ সম্পন্ন",
        taskType: "BILL",
        dueAt: 1702592000000,
        reminderAt: 1702500000000,
        completed: true,
        linkedLoanId: 3,
        clientCreatedAtMs: 1700000000000,
        clientUpdatedAtMs: 1700000008000,
        updatedAt: new Date(),
      })
    );
    await assertFails(taskRef.update({ taskType: "INVALID_TASK", updatedAt: new Date() }));
    await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("tasks").doc("task_7").delete());
    await assertSucceeds(taskRef.delete());
  });
});
