                                                                                                                                                            # TÀI LIỆU QUY TẮC NGHIỆP VỤ
## Thuê phòng – Hợp đồng – Hóa đơn – Thanh toán

## Định nghĩa và quy ước chung

### Thuật ngữ

| Thuật ngữ | Định nghĩa |
|---|---|
| Ngày ký | Ngày hai bên xác nhận hợp đồng. |
| Ngày nhận phòng | Ngày bàn giao phòng ghi trên hợp đồng; là ngày bắt đầu tính tiền thuê. |
| Kỳ thu | Chu kỳ thu tiền hằng tháng của Branch, mặc định từ ngày 1 đến ngày cuối tháng. |
| Kỳ lẻ | Phần kỳ thu không trọn tháng (vào ở, trả phòng hoặc chuyển phòng giữa kỳ). |
| Cọc giữ chỗ | Tiền khách đóng để giữ phòng/slot trước khi ký hợp đồng. |
| Cọc hợp đồng | Tiền đảm bảo nghĩa vụ trong suốt thời gian thuê. |
| Người-ngày | Số ngày mỗi người thực tế ở trong kỳ; dùng để chia chi phí phòng ở ghép. |
| Phiếu/yêu cầu thanh toán cọc giữ chỗ | Chứng từ yêu cầu khách đóng cọc cho Booking được hệ thống xác định đủ điều kiện và đã khóa tạm tài nguyên; tách biệt với hóa đơn nhận phòng và hóa đơn tháng. |
| Hợp đồng chờ hiệu lực | Hợp đồng đã được hai bên xác nhận nhưng chưa đáp ứng đầy đủ điều kiện để chuyển sang trạng thái "Đang hoạt động". |

### Quy tắc tính tiền theo ngày

| Mã      | Quy tắc |
|---------|---|
| BR-G01  | Số ngày ở = ngày cuối − ngày đầu + 1 (tính cả hai đầu). |
| BR-G02  | Tiền theo ngày = Giá tháng × Số ngày ở ÷ **Số ngày thực tế của tháng** (28/29/30/31). |
| BR-G03  | Tiền thuê và dịch vụ cố định tính theo ngày được làm tròn đến 1.000đ gần nhất, phần lẻ dưới 500đ làm tròn xuống, từ 500đ làm tròn lên. Chỉ làm tròn một lần sau khi tính xong từng khoản của từng ContractTenant; không làm tròn đơn giá theo ngày hoặc các bước trung gian. |
| BR-G03a | Tổng tiền điện nước của từng khoản nguồn được làm tròn đến đồng gần nhất, phần lẻ dưới 0,5đ làm tròn xuống, từ 0,5đ làm tròn lên. Khi chia cho các thành viên, áp dụng BR-I11 để tổng tiền phân bổ bằng đúng tổng tiền nguồn. |
| BR-G03b | Tổng hóa đơn bằng tổng các dòng tiền đã được xác định, không làm tròn thêm tổng hóa đơn. Tiền thanh toán thực nhận và các khoản cọc, hoàn tiền, cấn trừ đã xác định được giữ chính xác đến đồng; không tự làm tròn về 1.000đ. |
| BR-G04  | Ngày trả phòng/bàn giao được tính là ngày ở. Khi chuyển phòng vào ngày D, phòng cũ tính đến hết ngày D−1, phòng mới tính từ ngày D. |
| BR-G05  | Tiền cọc là khoản phải hoàn trả, không được tính vào doanh thu. |

---

## I. CHÍNH SÁCH THUÊ PHÒNG VÀ HỢP ĐỒNG

### Câu 1. Ngày bắt đầu tính tiền thuê là ngày ký hợp đồng hay ngày nhận phòng?

**Quyết định:** Tính từ **ngày nhận phòng**.

| Mã | Quy tắc |
|---|---|
| BR-R01 | Tiền thuê được tính từ ngày nhận phòng ghi trên hợp đồng, không phụ thuộc ngày ký. |
| BR-R02 | Ngày nhận phòng phải bằng hoặc sau ngày ký. Từ ngày ký đến ngày nhận phòng, phòng được giữ cho khách và không tính tiền thuê. |
| BR-R03 | Khách đến ở muộn hơn ngày nhận phòng: vẫn tính tiền từ ngày nhận phòng trên hợp đồng. |
| BR-R04 | Chủ trọ bàn giao muộn: ngày nhận phòng được điều chỉnh theo ngày bàn giao thực tế. |

**Ví dụ:** Ký ngày 20/10, nhận phòng ngày 01/11 → tính tiền từ 01/11. Khách đến ở ngày 05/11 vẫn tính từ 01/11.

---

### Câu 2. Khách có được thuê phòng vào bất kỳ ngày nào trong tháng không?

**Quyết định:** **Có.**

| Mã | Quy tắc |
|---|---|
| BR-R05 | Ngày nhận phòng có thể là bất kỳ ngày nào trong tháng. |
| BR-R06 | Phần thời gian từ ngày nhận phòng đến hết kỳ thu hiện tại được tính theo kỳ lẻ (Câu 3). Từ kỳ tiếp theo, hợp đồng thu theo kỳ chung của Branch. |
| BR-R07 | Ngày nhận phòng không được trùng thời gian với hợp đồng hoặc giữ chỗ khác trên cùng phòng (thuê nguyên phòng) hoặc cùng slot (thuê theo slot). |

---

### Câu 3. Nếu khách vào trọ giữa tháng thì tiền phòng tháng đầu tính như thế nào?

**Quyết định:** Tính **theo số ngày ở thực tế** của tháng đầu.

| Mã | Quy tắc |
|---|---|
| BR-R08 | Tiền thuê tháng đầu = Giá thuê × Số ngày ở ÷ Số ngày của tháng (BR-G02). |
| BR-R09 | Tiền thuê tháng đầu, dịch vụ cố định tháng đầu và phần cọc hợp đồng còn thiếu được thu trong một hóa đơn nhận phòng của ContractTenant tương ứng, theo nghĩa vụ đã xác định trong hợp đồng. Hóa đơn được lập và phát hành sau khi hợp đồng đã được hai bên xác nhận, trong giai đoạn chờ hiệu lực. Phần cọc còn thiếu được tính sau khi trừ cọc giữ chỗ đã chuyển sang cọc hợp đồng. Tháng lẻ dù ít ngày vẫn thu trong hóa đơn này, không gộp sang tháng sau. |
| BR-R10 | Điện nước của tháng đầu được tính từ chỉ số lúc bàn giao và thu trong hóa đơn của kỳ tiếp theo. |

**Ví dụ:** Giá thuê 3.000.000đ/tháng, Wi-Fi 100.000đ/tháng. Nhận phòng ngày 18/10 (tháng 10 có 31 ngày). Cọc hợp đồng 3.000.000đ, đã đóng cọc giữ chỗ 1.000.000đ.

| Khoản | Cách tính | Số tiền |
|---|---|---:|
| Tiền thuê 18/10 – 31/10 | 3.000.000 × 14 ÷ 31 = 1.354.839 | 1.355.000 |
| Wi-Fi 18/10 – 31/10 | 100.000 × 14 ÷ 31 = 45.161 | 45.000 |
| Cọc hợp đồng còn thiếu | 3.000.000 − 1.000.000 | 2.000.000 |
| **Tổng hóa đơn nhận phòng** | | **3.400.000** |

**Ví dụ:** Hợp đồng đang ở trạng thái chờ hiệu lực khi phát hành hóa đơn nhận phòng 3.400.000đ. Sau khi hệ thống xác minh khách đã thanh toán đủ và đã đến ngày bắt đầu thuê, hợp đồng chuyển sang Đang hoạt động và phòng được bàn giao. Tổng cọc hợp đồng lúc này là 3.000.000đ, gồm 1.000.000đ cọc giữ chỗ chuyển sang và 2.000.000đ đóng bổ sung.

---

### Câu 4. Nếu khách trả phòng giữa tháng thì tiền thuê còn lại xử lý ra sao?

**Quyết định:** Tiền thuê đã thu trước cho cả tháng. Xử lý phần chưa ở tùy theo khách **trả phòng hợp lệ** hay **vi phạm**.

| Mã      | Quy tắc                                                                                                                                                                                                                                                                                                                                                       |
|---------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| BR-R11  | **Trả phòng hợp lệ**: trả đúng hạn hợp đồng, hoặc trả trước hạn nhưng đã báo trước tối thiểu 30 ngày (mặc định), hoặc được chủ trọ đồng ý.                                                                                                                                                                                                                    |
| BR-R12  | **Trả phòng vi phạm**: tự ý chấm dứt trước hạn mà không báo trước đủ thời gian và không được chủ trọ đồng ý.                                                                                                                                                                                                                                                  |
| BR-R13  | Trả phòng hợp lệ: tính giảm và hoàn tiền thuê theo BR-R13a–R13c; dịch vụ cố định được tính lại theo cách tính tháng lẻ đã thỏa thuận của từng dịch vụ, không mặc định hoàn theo ngày cho mọi dịch vụ. Hoàn cọc sau khi trừ nghĩa vụ còn nợ theo Câu 9.                                                                                                                                                                                                                                            |
| BR-R13a | Khi trả phòng hợp lệ, tính lại tiền thuê phải trả cho những ngày đã ở trong khoảng thời gian của khoản thuê gốc, sử dụng giá và điều kiện đã thỏa thuận, làm tròn theo BR-G03. Khoản giảm tiền thuê = tiền thuê đã tính trên hóa đơn gốc − tiền thuê phải trả sau tính lại, tối thiểu bằng 0. Không tính và làm tròn độc lập phần tiền thuê chưa ở.|
| BR-R13b | Khoản giảm nghĩa vụ không mặc nhiên là tiền được hoàn. Chỉ hoàn phần tiền đã thanh toán và được xác minh còn dư sau khi trừ tiền thuê phải trả và các phần đã hoàn/cấn trừ trước đó. Nếu tiền đã thanh toán chưa đủ tiền thuê phải trả thì khách tiếp tục thanh toán phần thiếu.|
| BR-R13c | Mọi khoản giảm, hoàn hoặc cấn trừ phải tham chiếu hóa đơn gốc, ContractTenant và khoảng thời gian tương ứng. Cùng một phần tiền không được vừa hoàn vừa cấn trừ hoặc xử lý nhiều lần. Cọc được quyết toán riêng theo Câu 9.|
| BR-R14 | Trả phòng vi phạm: không giảm hoặc hoàn tiền thuê và phí dịch vụ cố định đã thu cho phần thời gian chưa sử dụng trong kỳ đã tính tiền. Điện nước và dịch vụ tính theo số lượng/lượt được quyết toán theo mức sử dụng thực tế thuộc trách nhiệm của người rời đi. Cọc thực còn của người vi phạm được dùng để thanh toán các khoản còn nợ; phần cọc còn lại sau cấn trừ bị khấu trừ toàn bộ. Nếu cọc không đủ thanh toán nợ, khách tiếp tục thanh toán phần thiếu. Việc khấu trừ phải được phê duyệt và ghi nhận theo BR-P17a–P17c. |
| BR-R15 | Khi trả phòng phải lập **hóa đơn quyết toán** cho ContractTenant rời đi, gồm điện nước đến thời điểm bàn giao theo các khoảng đã chốt và phần phân bổ đã duyệt tại BR-I07–I10a, chi phí hư hỏng do người đó chịu trách nhiệm đã được duyệt, nợ cũ, các khoản hoàn trả và hoàn cọc thuộc người đó. Chỉ đưa vào khoản điện nước chưa được tính trên hóa đơn khác. Nợ cũ được tham chiếu từ hóa đơn gốc, không ghi thành khoản nợ mới. Nếu tổng hợp theo phòng, phải thể hiện rõ phần của từng người. |
| BR-R16  | Quyết toán âm → chủ trọ hoàn tiền cho khách trong vòng 7 ngày lịch, mặc định tính từ ngày bản quyết toán được phê duyệt và thông báo cho khách. Quyết toán dương → khách thanh toán phần còn thiếu. Việc hoàn tiền thực hiện theo BR-P17a–P17c.                                                                                                                                                                                                                                 |

**Ví dụ – Trả phòng hợp lệ:** Tháng 11 (30 ngày) khách đã đóng tiền thuê 3.000.000đ và Wi-Fi 100.000đ. Khách báo trước đủ 30 ngày và ở đến hết 15/11. Cọc 3.000.000đ.

| Khoản | Cách tính | Số tiền |
|---|---|---:|
| Hoàn tiền thuê 16/11 – 30/11 | 3.000.000 × 15 ÷ 30 | −1.500.000 |
| Hoàn Wi-Fi 16/11 – 30/11 | 100.000 × 15 ÷ 30 | −50.000 |
| Điện 01/11 – 15/11 | 50 kWh × 3.500 | +175.000 |
| Nước 01/11 – 15/11 | 2 m³ × 18.000 | +36.000 |
| Thay khóa cửa (lỗi khách, đã duyệt) | | +150.000 |
| Hoàn cọc | | −3.000.000 |
| **Tổng quyết toán** | | **−4.189.000** |

→ Chủ trọ hoàn cho khách **4.189.000đ**.

**Ví dụ – Trả phòng vi phạm (cùng dữ liệu):** Không hoàn 1.550.000đ tiền thuê và Wi-Fi. Khoản nợ 361.000đ (175.000 + 36.000 + 150.000) được trừ vào cọc; 2.639.000đ cọc còn lại bị khấu trừ.

---

### Câu 5. Khi hết hạn hợp đồng, hệ thống tự gia hạn hay phải tạo hợp đồng mới?

**Quyết định:** **Không tự động gia hạn.** Gia hạn bằng **phụ lục** trên hợp đồng hiện tại; chỉ tạo hợp đồng mới khi thay đổi lớn.

| Mã      | Quy tắc |
|---------|---|
| BR-R17  | Hệ thống nhắc Owner/Manager và người đại diện trước ngày hết hạn 30 ngày. |
| BR-R18  | Gia hạn với điều khoản giữ nguyên hoặc chỉ thay đổi giá thuê → lập **phụ lục gia hạn**, cập nhật ngày kết thúc mới trên hợp đồng hiện tại. |
| BR-R19  | Thay đổi phòng, hình thức thuê (nguyên phòng/slot) hoặc người đại diện → kết thúc hợp đồng cũ và **tạo hợp đồng mới**; cọc được chuyển sang hợp đồng mới. |
| BR-R20  | Hết hạn mà không gia hạn và khách trả phòng → hợp đồng chuyển trạng thái "Hết hạn" và tiến hành quyết toán (Câu 4). |
| BR-R20a | Hết hạn mà khách vẫn tiếp tục ở, chưa gia hạn và chưa bàn giao trả phòng → hệ thống đánh dấu “Hết hạn nhưng còn ở”, tiếp tục tính tiền thuê và dịch vụ theo điều khoản cuối cùng đã được hai bên thống nhất. Không tự động gia hạn hoặc thay đổi ngày kết thúc hợp đồng. Hệ thống cảnh báo Owner/Manager khi hết hạn và nhắc lại mỗi tháng cho đến khi gia hạn hoặc trả phòng. |
| BR-R20b | Phòng/slot của người vẫn còn ở tiếp tục được tính là đang có người sử dụng, không tự chuyển thành trống hoặc cho khách khác giữ chỗ trùng thời gian. Với phòng ở ghép, theo dõi theo từng ContractTenant thực tế còn ở. |
| BR-R20c | Khi khách trả phòng sau ngày hết hạn, ghi nhận ngày bàn giao thực tế để tính tiền và quyết toán theo Câu 4 cùng chính sách dịch vụ tương ứng. Không tự động coi việc ở quá hạn là trả phòng vi phạm hoặc mất cọc; phí phạt chậm thanh toán, nếu có, áp dụng theo Câu 10. Khi lập phụ lục gia hạn, không tính trùng các khoản đã ghi nhận trong thời gian ở quá hạn. |

---

### Câu 6. Nếu khách chuyển phòng giữa tháng thì tiền phòng và hợp đồng xử lý thế nào?

**Quyết định:** Kết thúc hợp đồng cũ và lập hợp đồng mới cho phòng mới. Tiền thuê tính theo số ngày ở mỗi phòng và được cấn trừ.

| Mã | Quy tắc |
|---|---|
| BR-R21 | Chuyển phòng không bị xem là vi phạm: hợp đồng cũ kết thúc ở ngày D−1, hợp đồng mới bắt đầu từ ngày D. |
| BR-R22 | Tiền thuê phòng cũ của những ngày chưa ở (đã thu trước) được cấn trừ vào tiền thuê phòng mới cho phần còn lại của tháng. |
| BR-R23 | Điện nước phòng cũ được chốt chỉ số tại ngày chuyển và thu ngay khi chuyển phòng. |
| BR-R24 | Cọc hợp đồng cũ chuyển sang hợp đồng mới. Phòng mới có mức cọc cao hơn → khách đóng thêm phần chênh; thấp hơn → phần dư được cấn trừ kỳ sau hoặc hoàn. |
| BR-R25 | Chuyển slot trong cùng phòng với cùng giá không cần lập hợp đồng mới; chỉ cập nhật vị trí ở. |

**Ví dụ:** Phòng A 3.000.000đ → Phòng B 3.600.000đ, chuyển ngày 16/11 (tháng 30 ngày). Đã đóng tiền phòng A cả tháng 11. Cọc A 3.000.000đ, cọc B 3.600.000đ.

| Khoản | Cách tính | Số tiền |
|---|---|---:|
| Cấn trừ tiền thuê A 16/11 – 30/11 | 3.000.000 × 15 ÷ 30 | −1.500.000 |
| Tiền thuê B 16/11 – 30/11 | 3.600.000 × 15 ÷ 30 | +1.800.000 |
| Điện nước A 01/11 – 15/11 | 175.000 + 36.000 | +211.000 |
| Cọc bổ sung | 3.600.000 − 3.000.000 | +600.000 |
| **Khách phải đóng** | | **1.111.000** |

---

## II. CHÍNH SÁCH TÍNH TIỀN VÀ THANH TOÁN

### Câu 7. Ngày thu tiền phòng cố định hằng tháng hay theo ngày bắt đầu thuê?

**Quyết định:** **Cố định hằng tháng theo Branch.**

| Mã      | Quy tắc |
|---------|---|
| BR-P01  | Mỗi Branch có một kỳ thu chung cho tất cả phòng, mặc định từ ngày 1 đến cuối tháng. |
| BR-P02  | Hệ thống tự tạo hóa đơn tháng dạng nháp vào ngày 1. Owner/Manager kiểm tra và phát hành; hạn thanh toán mặc định ngày 5. Nếu phát hành muộn, hạn thanh toán không được sớm hơn ngày phát hành cộng 4 ngày lịch. Hóa đơn nháp chưa phát sinh nghĩa vụ thanh toán đến hạn đối với khách. |
| BR-P03  | Khách vào ở giữa tháng được tính kỳ lẻ ở tháng đầu, sau đó theo kỳ chung của Branch. |
| BR-P03a | Hạn thanh toán theo ngày được tính đến hết ngày đó, theo giờ Việt Nam (UTC+7). Thời hạn 24 giờ của phiếu cọc giữ chỗ được tính theo thời điểm tạo phiếu, không theo ngày lịch. |

**Lý do:** Chủ trọ chốt điện nước một lần cho cả dãy; phòng ở ghép dùng chung đồng hồ nên mọi người phải cùng kỳ để chia tiền; thuận tiện đối soát và thống kê doanh thu theo tháng.

---

### Câu 8. Tiền phòng thu trước cho kỳ tiếp theo hay thu sau khi kết thúc kỳ?

**Quyết định:** **Tiền phòng thu trước; điện nước thu sau.**

| Mã | Quy tắc |
|---|---|
| BR-P04 | Thu trước (cho tháng hiện tại): tiền thuê, dịch vụ cố định (Wi-Fi, rác, giữ xe…). |
| BR-P05 | Thu sau (của tháng trước): điện, nước, dịch vụ tính theo số lượng/lượt. |
| BR-P06 | Mỗi khoản trên hóa đơn phải ghi rõ kỳ áp dụng, vì một hóa đơn chứa khoản của hai tháng khác nhau. |
| BR-P07 | Cùng một ContractTenant, cùng một khoản nghĩa vụ và cùng kỳ sử dụng không được tính trùng trên các hóa đơn chưa hủy, kể cả khác loại hóa đơn. Hóa đơn thay thế hoặc khoản điều chỉnh phải tham chiếu khoản gốc để đối chiếu, không làm phát sinh thu trùng. |

**Ví dụ – Hóa đơn tháng 11 (phát hành 01/11, hạn 05/11):**

| Khoản | Kỳ áp dụng | Số tiền |
|---|---|---:|
| Tiền thuê | Tháng 11 | 3.000.000 |
| Wi-Fi | Tháng 11 | 100.000 |
| Điện: 90 kWh × 3.500 | Tháng 10 | 315.000 |
| Nước: 4 m³ × 18.000 | Tháng 10 | 72.000 |
| **Tổng** | | **3.487.000** |

---

### Câu 9. Tiền cọc được tính như thế nào? Khi nào hoàn cọc hoặc khấu trừ?

**Quyết định:** Có hai loại cọc. Cọc giữ chỗ được chuyển thành một phần cọc hợp đồng.

**A. Cọc giữ chỗ**

| Mã      | Quy tắc                                                                                                                                                                                                                                                                                        |
|---------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| BR-P08  | Mức cọc giữ chỗ mặc định 1.000.000đ (hoặc theo % giá thuê do Owner cấu hình).                                                                                                                                                                                                                  |
| BR-P09  | Khi khách yêu cầu cọc giữ chỗ, hệ thống tự kiểm tra khả dụng theo tài nguyên và khoảng thời gian thuê, không cần Owner/Manager duyệt. Nếu hợp lệ, hệ thống đồng thời khóa tạm tài nguyên và tạo phiếu thanh toán cọc giữ chỗ, có hạn 24 giờ kể từ thời điểm tạo.                               |
| BR-P10  | Trong thời gian khóa tạm, không cho khách khác tạo yêu cầu giữ chỗ có xung đột. Thuê nguyên phòng phải kiểm tra cả phòng và các slot; thuê theo slot phải kiểm tra slot được chọn và yêu cầu thuê nguyên phòng.                                                                                |
| BR-P10a | Khi khoản tiền đã được xác minh đủ cọc, Booking chuyển sang Đã xác nhận giữ chỗ. Nếu hết hạn mà chưa đủ, Booking hết hạn và tài nguyên được mở lại; phần đã đóng được hoàn theo chính sách.                                                                                                    |
| BR-P10b | Kiểm tra khả dụng và tạo khóa tạm phải thực hiện nguyên tử, có cơ chế khóa/ràng buộc tại database. Với các yêu cầu xung đột đồng thời, chỉ một yêu cầu được thành công và nhận phiếu thanh toán. Ẩn nút trên giao diện không thay thế kiểm tra tại backend.                                    |
| BR-P10c | Tiền đến sau khi Booking hết hạn được đưa vào xử lý ngoại lệ; không tự xác nhận giữ chỗ nếu tài nguyên đã được người khác giữ hoặc thuê.                                                                                                                                                       |
| BR-P11  | Booking đã xác nhận được giữ chỗ tối đa 14 ngày kể từ thời điểm xác nhận đủ cọc. Trong thời hạn này, khách phải hoàn tất ký và chuyển Booking sang hợp đồng. Nếu hết hạn mà chưa chuyển, Booking hết hạn, tài nguyên được giải phóng; cọc được xử lý theo nguyên nhân và chính sách tại mục B. |
| BR-P12 | Khi ký hợp đồng, cọc giữ chỗ đã được xác minh được chuyển thành một phần cọc hợp đồng, tối đa bằng nghĩa vụ cọc còn thiếu đã thỏa thuận của từng ContractTenant. Phần tiền vượt nghĩa vụ cọc được ghi nhận là tiền dư, xử lý theo BR-P27 và BR-P17a–P17c; không tự động chuyển sang thanh toán tiền thuê hoặc dịch vụ. Đây là chuyển mục đích sử dụng khoản tiền đã thu, không thu lại và không ghi nhận thêm một lần nhận tiền. Việc chuyển Booking sang hợp đồng phải đồng thời chuyển quyền giữ tài nguyên sang hợp đồng, không tạo khoảng trống để khách khác đặt xen vào. Booking đã chuyển sang hợp đồng không còn chịu hạn giữ chỗ 14 ngày. |

**B. Hoàn cọc giữ chỗ**

| Trường hợp | Hoàn cọc |
|---|---|
| Khách hủy trong vòng 24 giờ kể từ khi đóng cọc | 100% |
| Khách hủy sau 24 giờ hoặc không đến nhận phòng | Không hoàn |
| Chủ trọ từ chối hoặc không bàn giao được phòng | 100% |
| Hết hạn đóng cọc khi mới đóng một phần | Hoàn 100% phần đã đóng |
| Tiền cọc về sau khi yêu cầu đã hết hạn | Nếu phòng còn trống, Manager được khôi phục giữ phòng; nếu không, hoàn 100% |

**C. Cọc hợp đồng**

| Mã      | Quy tắc |
|---------|---|
| BR-P13  | Cọc hợp đồng mặc định bằng 1 tháng tiền thuê, Owner cấu hình 0–3 tháng. Thuê theo slot: tính theo giá thuê slot đã thỏa thuận. Thuê nguyên phòng có nhiều thành viên: tổng cọc phòng được phân bổ cho từng ContractTenant theo thỏa thuận, không mặc định chia đều. Mức cọc của từng người được ghi cố định trong hợp đồng; tổng các phần phải khớp tổng cọc đã thỏa thuận. |
| BR-P13a | Cọc giữ chỗ chuyển sang cọc hợp đồng phải được xác định thuộc ContractTenant nào. Nếu một khoản cọc giữ chỗ dùng cho nhiều thành viên, phải ghi rõ số tiền phân bổ cho từng người; mỗi phần không vượt nghĩa vụ cọc còn thiếu của người đó và tổng phân bổ không vượt khoản thực thu đã được xác minh. Nếu cọc hợp đồng bằng 0, toàn bộ cọc giữ chỗ được xử lý là tiền dư theo BR-P27. |
| BR-P13b | Một người được đóng cọc hộ người khác. Hệ thống ghi nhận riêng người thực trả và người có phần cọc được ghi nhận. Khi hoàn, tiền được hoàn cho người có phần cọc tương ứng; hoàn cho người nhận thay phải có xác nhận của người đó. |
| BR-P14 | Điều kiện bắt đầu thuê được kiểm tra riêng cho từng ContractTenant: hợp đồng đã được hai bên xác nhận, đã đến ngày bắt đầu thuê của người đó và hệ thống đã xác minh thanh toán đủ hóa đơn nhận phòng thuộc nghĩa vụ của người đó, gồm tiền thuê tháng đầu, dịch vụ cố định tháng đầu và phần cọc hợp đồng còn thiếu. Người chưa đáp ứng đủ điều kiện tiếp tục ở trạng thái chờ nhận phòng và chưa được bàn giao. |
| BR-P14a | Hợp đồng chung chuyển sang “Đang hoạt động” khi có ít nhất một ContractTenant đủ điều kiện bắt đầu thuê. Việc một thành viên chưa đóng đủ không ngăn thành viên khác đủ điều kiện nhận phòng; trạng thái hợp đồng chung không đồng nghĩa tất cả thành viên đã thanh toán đủ hoặc đã nhận phòng. |
| BR-P14b | Thuê nguyên phòng nhiều thành viên: nghĩa vụ tiền thuê, dịch vụ và cọc của từng người phải được xác định trong hợp đồng; tổng các phần phải khớp nghĩa vụ chung đã thỏa thuận. Việc một người chưa thanh toán hoặc chưa nhận phòng không tự động giảm tổng tiền thuê phòng, chuyển nợ sang người khác hoặc cho phép người ngoài giữ chỗ các vị trí trong phòng. |
| BR-P15  | Khi một thành viên trả phòng hợp lệ, hoàn phần cọc còn lại của người đó sau khi trừ nghĩa vụ riêng: điện nước kỳ cuối, chi phí hư hỏng đã duyệt và nợ còn tồn. Không dùng cọc của người này để trừ nợ của người khác nếu chưa có thỏa thuận được xác nhận. |
| BR-P16  | Khi một thành viên trả phòng vi phạm, áp dụng BR-R14 đối với phần cọc của người vi phạm; không tự khấu trừ phần cọc của các thành viên còn lại. |
| BR-P17  | Chi phí hư hỏng chung phải được xác định trách nhiệm và duyệt phần phân bổ cho từng thành viên trước khi khấu trừ cọc; không mặc định trừ toàn bộ vào người đại diện hoặc người rời phòng trước. |
| BR-P17a | Các khoản hoàn tiền, khấu trừ cọc và cấn trừ phải được Owner hoặc Manager có quyền tại Branch phê duyệt trước khi thực hiện. Ghi rõ người có quyền nhận tiền/người chịu nghĩa vụ, số tiền, lý do, nguồn tiền hoặc khoản nợ liên quan, người duyệt và thời điểm duyệt. Áp dụng cho cả cọc giữ chỗ và cọc hợp đồng. |
| BR-P17b | Chỉ sử dụng số dư cọc thực thu đã được xác minh, chưa hoàn hoặc sử dụng trước đó. Khấu trừ do hư hỏng phải có căn cứ xác định trách nhiệm và chi phí đã duyệt; không khấu trừ do hao mòn tự nhiên. Khi dùng cọc thanh toán nợ, đồng thời giảm số dư cọc và khoản nợ tương ứng, không tiếp tục thu lại phần nợ đã được cấn trừ. |
| BR-P17c | Phê duyệt hoàn tiền chưa đồng nghĩa đã hoàn tiền. Chỉ ghi nhận hoàn tất khi việc chi tiền được xác minh; lưu số tiền, người nhận, thời điểm, phương thức và chứng từ hoặc xác nhận nhận tiền. Cùng một phần tiền không được hoàn hoặc cấn trừ nhiều lần, kể cả khi nhiều người thao tác đồng thời. |

**Ví dụ:** Giá thuê 3.000.000đ → cọc hợp đồng 3.000.000đ. Khách đã đóng cọc giữ chỗ 1.000.000đ → khi ký hợp đồng chỉ đóng thêm 2.000.000đ.

---

### Câu 10. Nếu khách thanh toán trễ hạn thì xử lý thế nào? Có tính phí phạt không?

**Quyết định:** Quá hạn thì đánh dấu và nhắc nợ. **Giai đoạn hiện tại không áp dụng phí phạt chậm thanh toán.** BR-P19–P21 mô tả chính sách dự phòng, chưa được kích hoạt; chỉ áp dụng khi nhóm thống nhất triển khai và điều khoản tương ứng đã được thỏa thuận trong hợp đồng.

| Mã      | Quy tắc |
|---------|---|
| BR-P18  | Sau 23:59 ngày hạn mà còn nợ, hóa đơn được đánh dấu **Quá hạn** và gửi nhắc cho người còn nợ. |
| BR-P19  | Phí trễ hạn chỉ áp dụng nếu đã được thỏa thuận trong hợp đồng. Mặc định có 3 ngày lịch ân hạn sau ngày đến hạn; bắt đầu tính phí từ ngày tiếp theo nếu vẫn còn dư nợ thuộc khoản được phép tính phạt. Phiếu cọc giữ chỗ áp dụng quy tắc hết hạn Booking, không áp dụng phí này. |
| BR-P20  | Khi bật phí trễ hạn, chọn một trong hai hình thức, không áp dụng đồng thời: cố định 50.000đ, chỉ một lần cho mỗi hóa đơn gốc sau ân hạn; hoặc 0,1%/ngày trên dư nợ chịu phạt thực tế của từng ngày. |
| BR-P20a | Với phí theo ngày, dùng dư nợ chịu phạt cuối mỗi ngày, sau khi trừ các khoản thanh toán/cấn trừ hợp lệ. Trần phí cộng dồn bằng 5% dư nợ chịu phạt tại thời điểm bắt đầu tính phí; trần lấy phần nguyên đến đồng. Cộng phí trước, làm tròn một lần đến 1.000đ gần nhất theo BR-G03, nhưng không vượt trần. |
| BR-P20b | Ngày ghi giảm dư nợ căn cứ thời điểm nhận tiền được xác minh và khoản tiền được phân bổ đúng nghĩa vụ, không mặc định dùng ngày Owner/Manager bấm xác nhận. Tiền chưa được xác minh không được tự coi là đã thanh toán. |
| BR-P21  | Phí trễ hạn được ghi thành khoản riêng trên hóa đơn kỳ tiếp theo, tham chiếu hóa đơn gốc và khoảng thời gian tính phí; không sửa hóa đơn đã phát hành, không tính phí trên phí phạt. Nếu đã ghi nhận một phần phí, lần sau chỉ ghi phần phát sinh thêm; không thu lặp khi chạy lại tác vụ hoặc chuyển sang tháng mới. |
| BR-P22  | Owner/Manager được miễn giảm phí trễ hạn, có ghi lý do. |

**Ví dụ tham khảo khi chính sách phí phạt được kích hoạt; không áp dụng trong giai đoạn hiện tại.**

**Ví dụ:** Hóa đơn 3.487.000đ, hạn 05/11, ân hạn đến 08/11. Khách đóng 2.000.000đ ngày 04/11 và 1.487.000đ ngày 15/11.
- Phí cố định: hóa đơn tháng 12 có thêm khoản phí trễ hạn **50.000đ**.
- Phí theo ngày: 1.487.000 × 0,1% × 6 ngày (09/11 – 14/11) = 8.922 → **9.000đ**.

---

### Câu 11. Khách có được thanh toán từng phần hay bắt buộc thanh toán toàn bộ?

**Quyết định:** **Cho phép thanh toán từng phần** với hóa đơn. Riêng cọc giữ chỗ phải đóng đủ mới giữ phòng.

| Mã | Quy tắc |
|---|---|
| BR-P23 | Một hóa đơn được thanh toán nhiều lần; số đã thu là tổng các giao dịch đã xác nhận. |
| BR-P24 | Trạng thái thanh toán: Chưa thanh toán → Thanh toán một phần → Đã thanh toán. |
| BR-P25 | Phòng nhiều người thuê: mỗi thành viên có hóa đơn và công nợ riêng; người đại diện có thể thanh toán hộ cả phòng hoặc từng người tự thanh toán. Khi người đại diện đóng một phần cho cả phòng, tiền được phân bổ vào hóa đơn của từng thành viên theo tỷ lệ số còn nợ. Hệ thống ghi nhận riêng người thực trả và người được giảm nợ; không chuyển nghĩa vụ của các thành viên sang người đại diện. |
| BR-P26 | Một người được đóng hộ người khác; hệ thống ghi nhận riêng người chuyển tiền và người được trừ nợ. |
| BR-P27 | Tiền thừa không tự động trừ vào kỳ sau; được giữ là khoản dư chờ Owner/Manager quyết định cấn trừ kỳ sau hoặc hoàn trả. |

**Ví dụ:** Hóa đơn 3.487.000đ.

| Lần | Số tiền | Đã thu | Còn nợ | Trạng thái |
|---|---:|---:|---:|---|
| 1 | 2.000.000 | 2.000.000 | 1.487.000 | Thanh toán một phần |
| 2 | 1.500.000 | 3.500.000 | 0 | Đã thanh toán, dư 13.000đ |

---

### Câu 12. Nếu chủ trọ thay đổi giá thuê thì áp dụng ngay hay từ kỳ tiếp theo?

**Quyết định:** Áp dụng ngay cho **hợp đồng mới**; với hợp đồng đang hiệu lực, áp dụng **từ kỳ tiếp theo** sau thời gian báo trước.

| Mã | Quy tắc |
|---|---|
| BR-P28 | Giá phòng mới áp dụng ngay cho yêu cầu giữ phòng và hợp đồng tạo sau thời điểm thay đổi. |
| BR-P29 | Hợp đồng đang hiệu lực giữ nguyên giá đã ký. Muốn tăng giá phải lập phụ lục điều chỉnh giá và báo trước tối thiểu 30 ngày. |
| BR-P30 | Giá mới có hiệu lực từ ngày đầu của kỳ thu tiếp theo sau thời hạn báo trước; không đổi giá giữa một kỳ. |
| BR-P31 | Hóa đơn đã phát hành không thay đổi khi giá thay đổi. |

**Ví dụ:** Ngày 10/11, chủ trọ báo tăng giá từ 3.000.000đ lên 3.300.000đ. Sau 30 ngày là 10/12 → giá mới áp dụng từ kỳ **tháng 01**. Hóa đơn tháng 12 vẫn tính 3.000.000đ.

---

## III. CHÍNH SÁCH HÓA ĐƠN VÀ DỊCH VỤ

### Câu 13. Khi nào hệ thống tạo hóa đơn? Tự động hay chủ trọ tạo thủ công?

**Quyết định:** Hệ thống **tự lập hóa đơn nháp**; Owner/Manager **kiểm tra và phát hành**.

| Mã      | Quy tắc |
|---------|---|
| BR-I01 | **Hóa đơn nhận phòng**: lập cho ContractTenant tương ứng sau khi hợp đồng đã được hai bên xác nhận và người đó còn ở trạng thái chờ nhận phòng. Các khoản thu phải thuộc nghĩa vụ đã xác định của người đó. Owner/Manager phụ trách Branch kiểm tra, phát hành để khách thanh toán trước khi người đó đủ điều kiện bắt đầu thuê và được bàn giao theo BR-P14–P14b. Hợp đồng chung có thể đang chờ hiệu lực hoặc đã hoạt động do thành viên khác bắt đầu thuê. |
| BR-I02 | **Hóa đơn tháng**: tự tạo dạng nháp vào ngày 1 hằng tháng cho từng ContractTenant đang thuê thuộc hợp đồng đang hiệu lực hoặc trường hợp “Hết hạn nhưng còn ở” trong Branch. Không tạo hóa đơn tháng thông thường cho người chưa bắt đầu thuê hoặc đã bàn giao trả phòng; các nghĩa vụ còn tồn của họ được xử lý qua hóa đơn bổ sung hoặc quyết toán tương ứng. Chỉ đưa tiền điện nước tháng trước vào khi số liệu đã được chốt và phần phân bổ đã được duyệt nếu có; loại trừ các khoản đã được tính trên hóa đơn nhận phòng, chuyển phòng, bổ sung hoặc quyết toán. Owner/Manager kiểm tra và phát hành; hóa đơn nháp chưa phải yêu cầu thanh toán gửi cho khách. |
| BR-I03  | **Hóa đơn chuyển phòng / quyết toán**: lập cho từng ContractTenant có phát sinh chuyển phòng hoặc trả phòng, thể hiện riêng các khoản phải thu, được cấn trừ và được hoàn. Nếu hiển thị tổng hợp theo phòng, vẫn phải truy được nghĩa vụ và nguồn tiền của từng người. |
| BR-I04  | Hóa đơn ở trạng thái nháp được sửa tự do; chỉ hóa đơn đã phát hành mới gửi cho khách và có mã thanh toán VietQR. Owner hoặc Manager phụ trách Branch được quyền phát hành. |
| BR-I05  | Mỗi ContractTenant có tối đa một hóa đơn tháng chưa hủy cho cùng một kỳ thu. Phòng có nhiều thành viên được tổng hợp hóa đơn trên màn hình quản lý, nhưng nghĩa vụ và công nợ vẫn được theo dõi riêng từng người. Hóa đơn nhận phòng, bổ sung và quyết toán được phân biệt với hóa đơn tháng; không được thu trùng cùng một khoản nghĩa vụ. |
| BR-I06  | Khoản điện nước chưa chốt số liệu hoặc chưa duyệt phân bổ thì chưa được đưa vào hóa đơn. Nếu hóa đơn còn nháp, bổ sung khoản phí sau khi nguồn đủ điều kiện rồi kiểm tra lại trước khi phát hành. Nguồn chưa chốt phải được theo dõi, không được bỏ sót. |
| BR-I06a | Nếu nguồn phí đủ điều kiện sau khi hóa đơn đã phát hành, lập hóa đơn bổ sung, không sửa hóa đơn cũ. Hóa đơn bổ sung ghi rõ nguồn, kỳ sử dụng và ContractTenant chịu nghĩa vụ; hạn thanh toán mặc định là ngày phát hành cộng 4 ngày lịch. Áp dụng BR-P07 để chống thu trùng. |

---

### Câu 14. Tiền điện, nước tính theo tháng lịch hay theo kỳ thuê của từng phòng?

**Quyết định:** Tính theo **tháng lịch** (kỳ chốt chỉ số chung của Branch), không theo kỳ thuê riêng của từng phòng.

| Mã | Quy tắc |
|---|---|
| BR-I07 | Chỉ số điện nước được chốt vào ngày cuối tháng cho tất cả phòng trong Branch. Ngoài lần chốt cuối tháng, phải chốt bổ sung tại thời điểm bàn giao vào, trả phòng hoặc chuyển phòng làm thay đổi thành viên sử dụng chung đồng hồ. |
| BR-I08 | Khách vào ở giữa tháng: ghi nhận chỉ số lúc bàn giao. Khách rời đi hoặc chuyển phòng giữa tháng: ghi nhận chỉ số tại thời điểm bàn giao tương ứng. Các lần chốt phải nối tiếp nhau, có ngày giờ và căn cứ xác nhận, không bỏ sót hoặc tính trùng lượng tiêu thụ giữa hai khoảng. |
| BR-I09 | Tiền điện nước của từng khoảng = (Chỉ số cuối khoảng − Chỉ số đầu khoảng) × Đơn giá áp dụng. Không tiêu thụ thì không tính tiền. Tổng tiền từng khoản nguồn được làm tròn theo BR-G03a. |
| BR-I10 | Phòng ở ghép: điện nước được tính và phân bổ riêng cho từng khoảng giữa các lần chốt chỉ số. Mỗi khoảng chỉ phân bổ cho những ContractTenant có sử dụng phòng trong khoảng đó, theo tỷ lệ người-ngày; trường hợp có cách chia khác đã được Owner/Manager duyệt thì áp dụng cách chia được duyệt. Tổng phần phân bổ phải khớp tiền nguồn của khoảng theo BR-I11. |
| BR-I10a | Khi một thành viên rời đi, phần điện nước của người đó trong các khoảng đã chốt và được duyệt được đưa vào quyết toán. Cuối tháng chỉ lập khoản thu đối với phần nghĩa vụ chưa được tính trên hóa đơn khác; không chia lại toàn bộ tháng làm phát sinh thu trùng hoặc thay đổi khoản đã quyết toán. Nếu phát hiện sai số liệu hoặc phân bổ đã phát hành, xử lý điều chỉnh theo Câu 16. |
| BR-I11 | Tổng tiền phân bổ cho các thành viên phải bằng đúng tổng tiền nguồn. Tính phần của từng người theo tỷ lệ đã duyệt; các thành viên không phải đại diện được lấy phần nguyên đến đồng, người đại diện nhận phần còn lại. Không làm tròn từng phần lên 1.000đ. |
| BR-I12 | Thành viên được đề nghị chia lại theo mức dùng thực tế; chỉ cách chia được Owner/Manager duyệt mới dùng để lập hóa đơn. |

**Ví dụ – Phòng ở ghép có thay đổi thành viên:** A và B ở cả tháng 11; C nhận phòng ngày 16/11. Chốt chỉ số cuối ngày 15/11 và cuối ngày 30/11. Giả sử mỗi khoảng sử dụng 150 kWh điện và 6 m³ nước, với giá điện 3.500đ/kWh và nước 18.000đ/m³.

Tiền mỗi khoảng = 150 × 3.500 + 6 × 18.000 = **633.000đ**.

| Khoảng sử dụng | Người sử dụng | Tổng tiền nguồn | A | B | C |
|---|---|---:|---:|---:|---:|
| 01/11 – 15/11 | A, B; mỗi người 15 ngày | 633.000 | 316.500 | 316.500 | 0 |
| 16/11 – 30/11 | A, B, C; mỗi người 15 ngày | 633.000 | 211.000 | 211.000 | 211.000 |
| **Tổng tháng** | | **1.266.000** | **527.500** | **527.500** | **211.000** |

Nếu một phần nghĩa vụ đã được tính trên hóa đơn trước đó, hóa đơn cuối tháng chỉ thu phần chưa được tính.

---

### Câu 15. Các dịch vụ cố định (Wi-Fi, rác, giữ xe…) có tính theo số ngày sử dụng không?

**Quyết định:** Tùy theo từng dịch vụ, Owner cấu hình một trong bốn cách tính cho tháng lẻ.

| Cách tính tháng lẻ | Ý nghĩa | Áp dụng mặc định |
|---|---|---|
| Theo ngày | Tính theo số ngày sử dụng (BR-G02) | Wi-Fi, rác, vệ sinh chung |
| Nửa tháng | Dùng ≤ 15 ngày tính 50%; > 15 ngày tính 100% | — |
| Trọn tháng | Có dùng trong tháng là tính đủ tháng | Giữ xe (vé tháng) |
| Không tính | Tháng lẻ không thu | — |

| Mã      | Quy tắc |
|---------|---|
| BR-I13  | Đăng ký dịch vụ giữa tháng: tính phí từ ngày được duyệt theo cách tính của dịch vụ đó. |
| BR-I14  | Khi khách vẫn tiếp tục thuê, việc hủy dịch vụ tùy chọn có hiệu lực từ đầu tháng sau; không hoàn tiền phần còn lại của tháng đã thu. Khi khách kết thúc thuê hợp lệ, áp dụng quy tắc quyết toán dịch vụ tại BR-I15a–I15b. |
| BR-I15  | Dịch vụ tính theo đầu người được ghi nhận nghĩa vụ riêng cho từng ContractTenant. Người vào/ra giữa tháng được tính phí theo cách tính tháng lẻ của dịch vụ đã thỏa thuận, không mặc định luôn tính theo ngày. |
| BR-I15a | Khi kết thúc thuê hợp lệ, tính lại phí dịch vụ trong kỳ: theo ngày → tính đến hết ngày trả phòng; nửa tháng → dùng không quá 15 ngày tính 50%, trên 15 ngày tính 100%; trọn tháng → có sử dụng thì tính đủ tháng; không tính tháng lẻ → không thu cho kỳ lẻ. Áp dụng giá và cấu hình đã chốt cho kỳ đó. |
| BR-I15b | Khoản giảm dịch vụ bằng phí gốc trừ phí phải trả sau tính lại, tối thiểu bằng 0. Việc hoàn tiền căn cứ số đã thanh toán và được xác minh, trừ phần đã hoàn/cấn trừ; áp dụng nguyên tắc chống hoàn trùng tại BR-R13b–R13c. Với dịch vụ chung theo phòng, phần nghĩa vụ từng người phải căn cứ kết quả phân bổ được duyệt. |
| BR-I16  | Điện, nước là dịch vụ mặc định, khách không được hủy. |

**Ví dụ:** Khách nhận phòng ngày 18/10 (ở 14/31 ngày).

| Dịch vụ | Giá tháng | Cách tính | Số tiền tháng 10 |
|---|---:|---|---:|
| Wi-Fi | 100.000 | Theo ngày: 100.000 × 14 ÷ 31 | 45.000 |
| Rác | 20.000 | Nửa tháng (14 ≤ 15 ngày) | 10.000 |
| Giữ xe | 120.000 | Trọn tháng | 120.000 |

---

### Câu 16. Nếu hóa đơn đã phát hành nhưng phát hiện sai tiền thì chỉnh sửa hoặc xử lý thế nào?

**Quyết định:** **Không sửa trực tiếp** hóa đơn đã phát hành. Cách xử lý tùy khách đã thanh toán hay chưa.

| Mã | Quy tắc |
|---|---|
| BR-I17 | Hóa đơn **chưa được thanh toán**: hủy hóa đơn sai (ghi lý do) và phát hành hóa đơn thay thế; mã thanh toán của hóa đơn cũ hết hiệu lực. |
| BR-I18 | Hóa đơn đã thanh toán một phần hoặc toàn bộ không được hủy. Lập khoản điều chỉnh tăng hoặc giảm, tham chiếu hóa đơn và khoản gốc. Khoản điều chỉnh được đưa vào hóa đơn tháng sau; nếu khách đã kết thúc thuê hoặc không còn hóa đơn kỳ sau, xử lý trên chứng từ điều chỉnh hoặc quyết toán riêng. Không sửa trực tiếp hóa đơn gốc. |
| BR-I19 | Thu thiếu cần thu ngay: lập hóa đơn bổ sung. Điều chỉnh giảm trước hết làm giảm nghĩa vụ còn nợ; chỉ phần tiền thực thanh toán còn dư mới được hoàn hoặc cấn trừ theo lựa chọn đã xác nhận với khách. Việc thực hiện phải được phê duyệt và ghi nhận theo BR-P17a–P17c. |
| BR-I20 | Mọi điều chỉnh phải ghi: hóa đơn gốc, khoản sai, số tiền đúng, lý do, người lập, người duyệt. |
| BR-I21 | Khách chuyển tiền vào hóa đơn đã hủy: giao dịch được đưa vào danh sách chờ xử lý để Manager gán sang hóa đơn thay thế. |

**Ví dụ:**

| Tình huống | Xử lý |
|---|---|
| Hóa đơn tháng 11 ghi sai tiền thuê 3.300.000đ (đúng là 3.000.000đ), khách chưa trả | Hủy hóa đơn, phát hành hóa đơn mới 3.487.000đ |
| Nhập sai chỉ số nước, tính 14 m³ thay vì 4 m³; khách đã trả đủ | Hóa đơn tháng 12 có khoản điều chỉnh giảm 10 × 18.000 = **−180.000đ** |
| Quên tính Wi-Fi tháng 11; khách đã trả đủ | Hóa đơn tháng 12 có khoản điều chỉnh tăng **+100.000đ** |

---