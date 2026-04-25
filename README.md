# Event-Management-System
Spring Boot  •  Thymeleaf  •  MySQL  •  Maven




        <script>
            function showServerBookingDetails(day) {
                const yearSelect = document.getElementById('yearSelect');
                const monthSelect = document.getElementById('monthSelect');
                const year = yearSelect ? yearSelect.value : new Date().getFullYear();
                const month = monthSelect ? monthSelect.value : new Date().getMonth() + 1;

                const monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
                document.getElementById('selectedDateDisplay').textContent = `${monthNames[month - 1]} ${day}, ${year}`;

                const container = document.getElementById('bookingDetailsContainer');
                const emptyMsg = document.getElementById('emptyDetailMessage');
                container.innerHTML = '';

                const data = document.getElementById('bookings-day-' + day);
                if (data) {
                    data.querySelectorAll('.booking-item-data').forEach(item => {
                        const title = item.getAttribute('data-title');
                        const venue = item.getAttribute('data-venue');
                        const time = item.getAttribute('data-time');
                        const society = item.getAttribute('data-society');
                        const status = item.getAttribute('data-status');
                        const note = item.getAttribute('data-note');

                        const div = document.createElement('div');
                        div.className = 'booking-item';
                        div.innerHTML = `
                            <div class="mb-3">
                                <span class="badge-venue mb-2 d-inline-block">${venue}</span>
                                <h6 class="fw-bold mb-1">${title}</h6>
                                <div class="extra-small text-muted mb-2">
                                    <i class="bi bi-clock me-1"></i> ${time}
                                </div>
                                <div class="extra-small text-muted mb-1">
                                    <strong>Society:</strong> ${society}
                                </div>
                                ${(note && note !== 'null' && note !== '') ? `<div class="mt-2 pt-2 border-top extra-small text-muted fst-italic">"${note}"</div>` : ''}
                            </div>
                        `;
                        container.appendChild(div);
                    });
                    emptyMsg.style.display = 'none';
                    container.style.display = 'block';
                } else {
                    emptyMsg.style.display = 'block';
                    container.style.display = 'none';
                }
            }
        </script>
