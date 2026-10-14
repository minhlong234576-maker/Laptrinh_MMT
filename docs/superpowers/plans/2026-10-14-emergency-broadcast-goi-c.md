# Emergency Broadcast — Gói C (Full UX + Ổn định) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Sửa lỗi logic chết người, hết freeze UI, chạy 1-click trên máy trường.

**Architecture:** Giữ nguyên Swing + UDP Multicast 239.10.10.10:5000 + MySQL trung tâm; chỉ sửa tại nguồn lỗi, thêm lớp config ngoài, đưa mọi truy vấn DB ra khỏi EDT.

**Tech Stack:** Java Swing, JDBC MySQL Connector/J 8.3.0, UDP Multicast, XAMPP MySQL.

## Global Constraints

- Không đổi giao thức multicast (IP/port giữ nguyên để tương thích máy trạm cũ).
- Không đổi schema DB (chỉ làm seed idempotent).
- Mọi thay đổi DB đều qua SwingWorker, cập nhật Swing chỉ trên EDT.
- YAGNI: không thêm thư viện mới, không đổi build system.

---

### Task 1: Config DB ngoài + DatabaseConnection fallback

**Files:**
- Create: `db.properties`
- Modify: `src/emergencybroadcast/database/DatabaseConnection.java`

**Interfaces:**
- Consumes: file `db.properties` (host,port,db,user,password)
- Produces: `DatabaseConnection.getConnection()` giữ nguyên signature, đọc config nếu có.

- [ ] **Step 1: Write the failing test**

```python
# scripts/check_config.py — kiểm tra DatabaseConnection.java có đọc db.properties không
import pathlib
src = pathlib.Path("src/emergencybroadcast/database/DatabaseConnection.java").read_text(encoding="utf-8")
assert "db.properties" in src, "chưa đọc db.properties"
print("PASS: đọc config ngoài")
```

- [ ] **Step 2: Run test to verify it fails**

Run: `python scripts/check_config.py`
Expected: FAIL với "chưa đọc db.properties"

- [ ] **Step 3: Write minimal implementation**

`db.properties`:
```properties
db.host=localhost
db.port=3306
db.name=emergency_broadcast
db.user=root
db.password=
```

`DatabaseConnection.java`: thêm static block đọc `db.properties` ( working dir, rồi classpath ), helper `getProp(key, def)`, dựng URL từ config. Giữ hằng số cũ làm default. Thêm `getStatusMessage()` trả về chuỗi trạng thái để UI hiển thị.

- [ ] **Step 4: Run test to verify it passes**

Run: `python scripts/check_config.py`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add db.properties src/emergencybroadcast/database/DatabaseConnection.java scripts/check_config.py
git commit -m "feat: externalize DB config to db.properties with fallback"
```

### Task 2: Fix QuickAlert gửi UDP khi DB fail + trùng alert_code

**Files:**
- Modify: `src/emergencybroadcast/QuickAlertFrame.java`
- Modify: `src/emergencybroadcast/SenderFrame.java` (hàm sinh code)

**Interfaces:**
- Consumes: `AlertDAO.insertAlert()` trả về id (>0 ok, -1 fail)
- Produces: chỉ gọi `MulticastSender.sendAlert()` khi id > 0.

- [ ] **Step 1: Write the failing test**

```python
import pathlib
q = pathlib.Path("src/emergencybroadcast/QuickAlertFrame.java").read_text(encoding="utf-8")
# phải có nhánh chặn gửi khi id<=0
assert "id <= 0" in q or "id > 0" in q, "chưa chặn gửi khi DB fail"
# đoạn gửi phải nằm trong nhánh id>0: kiểm tra thứ tự đơn giản
assert q.index("insertAlert") < q.index("sendAlert"), "thứ tự sai"
print("PASS")
```

- [ ] **Step 2: Run test to verify it fails**

Run: `python scripts/check_quickalert_guard.py`
Expected: FAIL (bản cũ gửi bất kể id)

- [ ] **Step 3: Write minimal implementation**

Hàm sinh code chung, duy nhất:
```java
private static String newAlertCode(String prefix) {
    return prefix + "-" + new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new java.util.Date())
        + "-" + (100 + new java.util.Random().nextInt(900));
}
```
Sender dùng `newAlertCode("ALT")`, Quick dùng `newAlertCode("QUICK")`. Trong QuickAlert worker: sau `insertAlert`, nếu `alertId <= 0` thì `invokeLater(setStatusError("Lỗi lưu DB..."))` và `return` (không gọi sendAlert).

- [ ] **Step 4: Run test to verify it passes**

Run: `python scripts/check_quickalert_guard.py` + `javac` biên dịch (Task 7).
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/emergencybroadcast/QuickAlertFrame.java src/emergencybroadcast/SenderFrame.java
git commit -m "fix: block multicast when DB insert fails, unique alert_code"
```

### Task 3: Fix Receiver receipt ID=1 mặc định + chống trùng lặp

**Files:**
- Modify: `src/emergencybroadcast/network/MulticastReceiver.java`

**Interfaces:**
- Consumes: chuỗi UDP `ALERT_ID`
- Produces: chỉ insert receipt khi `alertId > 0` và chưa thấy `alertCode` trong 30s.

- [ ] **Step 1: Write the failing test**

```python
import pathlib
s = pathlib.Path("src/emergencybroadcast/network/MulticastReceiver.java").read_text(encoding="utf-8")
assert "alertId = 1" not in s and "alertId=1" not in s.replace(" ",""), "còn default ID=1"
print("PASS")
```

- [ ] **Step 2: Run test to verify it fails**

Run: `python scripts/check_receiver_guard.py`
Expected: FAIL

- [ ] **Step 3: Write minimal implementation**

Trong `saveAlertReceiptToDB`: `if (alertId <= 0) { log + return; }` (không default 1). Thêm `Set<String> recentCodes` + timestamp, `parseRawMessageToAlert` giữ nguyên nhưng `run()` bỏ qua nếu `alertCode` đã nhận trong 30s. Dùng `ConcurrentHashMap<String,Long>`.

- [ ] **Step 4: Run test to verify it passes**

Run: `python scripts/check_receiver_guard.py`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/emergencybroadcast/network/MulticastReceiver.java
git commit -m "fix: skip receipt when alertId invalid, dedup by alertCode 30s"
```

### Task 4: Hết freeze UI — DB ra khỏi EDT

**Files:**
- Modify: `src/emergencybroadcast/HistoryFrame.java` (`loadHistoryAlerts`, `performFilter`)
- Modify: `src/emergencybroadcast/SenderFrame.java` (`loadAlertHistory`)
- Modify: `src/emergencybroadcast/ReceiverFrame.java` (`loadReceiptHistory`)

**Interfaces:**
- Consumes: DAO `getAll*`, `searchAlertsFiltered`
- Produces: `DefaultTableModel` chỉ đụng trên EDT (`done()`).

- [ ] **Step 1: Write the failing test**

```python
import pathlib, re
h = pathlib.Path("src/emergencybroadcast/HistoryFrame.java").read_text(encoding="utf-8")
# performFilter phải có SwingWorker và cập nhật model trong done(), không trong doInBackground
assert "SwingWorker" in h
assert "done()" in h
print("PASS pattern")
```

- [ ] **Step 2: Run test to verify it fails**

Run: `python scripts/check_edt.py`
Expected: FAIL ở bản cũ (loadHistoryAlerts đụng model trong doInBackground)

- [ ] **Step 3: Write minimal implementation**

Mẫu chuẩn cho cả 3 frame:
```java
new SwingWorker<List<Alert>,Void>() {
  protected List<Alert> doInBackground() { return alertDAO.searchAlertsFiltered(k,t,s); }
  protected void done() {
    try { List<Alert> rows = get(); tableModel.setRowCount(0); for (...) tableModel.addRow(...); }
    catch (Exception ex) { ... }
  }
}.execute();
```
`loadAlertHistory()` / `loadReceiptHistory()` bọc tương tự; nút Refresh disable trong lúc tải.

- [ ] **Step 4: Run test to verify it passes**

Run: `python scripts/check_edt.py`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/emergencybroadcast/HistoryFrame.java src/emergencybroadcast/SenderFrame.java src/emergencybroadcast/ReceiverFrame.java
git commit -m "fix: move DB queries off EDT, update tables on EDT"
```

### Task 5: Login phân biệt lỗi DB + hiện trạng thái DB

**Files:**
- Modify: `src/emergencybroadcast/LoginFrame.java`
- Modify: `src/emergencybroadcast/dao/UserDAO.java` (thêm `loginOrThrow` hoặc giữ `login` + gọi `testConnection` trước)

**Interfaces:**
- Consumes: `DatabaseConnection.testConnection()`
- Produces: `lblError` hiện "Không kết nối được MySQL/XAMPP..." khi DB down, kèm label trạng thái.

- [ ] **Step 1: Write the failing test**

```python
import pathlib
s = pathlib.Path("src/emergencybroadcast/LoginFrame.java").read_text(encoding="utf-8")
assert "testConnection" in s, "chưa kiểm tra DB riêng"
print("PASS")
```

- [ ] **Step 2: Run test to verify it fails** — như trên.

- [ ] **Step 3: Write minimal implementation**

Trong `performLogin()`: trước `userDAO.login`, gọi `DatabaseConnection.testConnection()` (nhanh, timeout mặc định). Nếu false → `lblError.setText("Không kết nối MySQL! Hãy bật XAMPP → Start MySQL...")` + return. Thêm `lblDbStatus` nhỏ dưới form, cập nhật khi mở frame bằng SwingWorker.

- [ ] **Step 4: Run test to verify it passes** — script trên PASS.

- [ ] **Step 5: Commit**

```bash
git add src/emergencybroadcast/LoginFrame.java
git commit -m "feat: login distinguishes DB-down from wrong password, shows DB status"
```

### Task 6: History lọc theo Loại + CSV chuẩn + Receiver chống spam popup

**Files:**
- Modify: `src/emergencybroadcast/HistoryFrame.java`
- Modify: `src/emergencybroadcast/ReceiverFrame.java`

**Interfaces:**
- Consumes: `AlertDAO.searchAlertsFiltered(keyword, alertType, severity)`
- Produces: CSV có quote `"` đúng chuẩn, popup gộp.

- [ ] **Step 1: Write the failing test**

```python
import pathlib
h = pathlib.Path("src/emergencybroadcast/HistoryFrame.java").read_text(encoding="utf-8")
assert "cmbTypeFilter" in h or "TypeFilter" in h, "chưa có lọc loại"
print("PASS")
```

- [ ] **Step 2: Run test to verify it fails** — FAIL bản cũ.

- [ ] **Step 3: Write minimal implementation**

History: thêm `JComboBox cmbTypeFilter {-- Tất cả --, FIRE, SECURITY, POWER, GAS, FLOOD, OTHER}` vào filterPanel; `performFilter/loadHistoryAlerts` truyền type vào `searchAlertsFiltered(keyword, typeOrNull, severityOrNull)`. CSV: hàm `csvCell(o)` bọc `"`, escape `""`, giữ BOM. Receiver: thêm flag `popupShowing`; `showAlertPopup` nếu đang hiện thì chỉ cập nhật text + beep, không mở dialog mới; dùng `JDialog` non-modal thay cho `showMessageDialog` khi CRITICAL liên tiếp < 3s.

- [ ] **Step 4: Run test to verify it passes**

- [ ] **Step 5: Commit**

```bash
git add src/emergencybroadcast/HistoryFrame.java src/emergencybroadcast/ReceiverFrame.java
git commit -m "feat: history filter by type, RFC4180 CSV, coalesced receiver popups"
```

### Task 7: 1-click chạy + seed idempotent + hạ javac 17

**Files:**
- Create: `run.bat`, `README.md` (viết lại)
- Modify: `database.sql`, `nbproject/project.properties`

**Interfaces:**
- Consumes: `lib/mysql-connector-j-8.3.0.jar`
- Produces: double-click `run.bat` là chạy được sau khi bật XAMPP.

- [ ] **Step 1: Write the failing test**

```bat
REM run.bat phải tồn tại và chứa javac/java + classpath lib
```

- [ ] **Step 2: Run test to verify it fails** — `Test-Path run.bat` = False.

- [ ] **Step 3: Write minimal implementation**

`run.bat`: kiểm tra `java -version`, `javac` build vào `build/classes-manual`, copy `.form` nếu cần, chạy `emergencybroadcast.Main` với classpath `lib/*`. `database.sql`: `INSERT IGNORE INTO users/alerts/receipts` + `SELECT @@...`. `project.properties`: `javac.source=17`, `javac.target=17`. README: 5 bước XAMPP → import SQL → run.bat → admin/admin123.

- [ ] **Step 4: Run test to verify it passes**

Run: `javac -encoding UTF-8 -cp lib/... -d build/classes-manual <all java>` → exit 0; `Test-Path run.bat` → True.

- [ ] **Step 5: Commit**

```bash
git add run.bat README.md database.sql nbproject/project.properties
git commit -m "chore: 1-click run.bat, idempotent seed, target Java 17"
```
