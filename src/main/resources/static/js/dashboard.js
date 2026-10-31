// Данные страницы лежат в блоке JSON: политика CSP запрещает встроенные скрипты.
const {categories, categoryCounts, merchants, merchantTypes, people, monthly} = JSON.parse(document.getElementById('dashboard-data').textContent);
const palette = ['#1d6a96', '#2f855a', '#d69e2e', '#805ad5', '#dd6b20', '#e53e3e', '#3182ce'];

new Chart(document.getElementById('categoriesChart'), {
    type: 'doughnut',
    data: {labels: categories.map(item => item.categoryName), datasets: [{data: categories.map(item => item.amount), backgroundColor: palette}]},
    options: {plugins: {legend: {position: 'bottom'}}}
});
new Chart(document.getElementById('monthlyChart'), {
    type: 'bar',
    data: {labels: monthly.map(item => item.complete ? item.month : item.month + ' (неполный)'), datasets: [
        {label: 'Доход, отмеченный вами', data: monthly.map(item => item.report.income), backgroundColor: '#2f855a'},
        {label: 'Расходы на покупки', data: monthly.map(item => item.report.expense), backgroundColor: '#e53e3e'},
        {label: 'Наличные', data: monthly.map(item => item.report.cashWithdrawals), backgroundColor: '#d69e2e'},
        {label: 'Списания с неизвестным назначением', data: monthly.map(item => item.report.unknownOutflow), backgroundColor: '#a0aec0'}
    ]}, options: {responsive: true}
});
new Chart(document.getElementById('merchantsChart'), {
    type: 'bar',
    data: {labels: merchants.map(item => item.merchant), datasets: [{label: 'Расходы', data: merchants.map(item => item.amount), backgroundColor: '#1d6a96'}]},
    options: {indexAxis: 'y', plugins: {legend: {display: false}}}
});
new Chart(document.getElementById('merchantTypesChart'), {
    type: 'doughnut',
    data: {labels: merchantTypes.map(item => item.merchantTypeName), datasets: [{data: merchantTypes.map(item => item.amount), backgroundColor: palette}]},
    options: {plugins: {legend: {position: 'bottom'}}}
});
new Chart(document.getElementById('peopleChart'), {
    type: 'bar',
    data: {labels: people.map(item => item.recipient), datasets: [{label: 'Переводы', data: people.map(item => item.amount), backgroundColor: '#6f42c1'}]},
    options: {indexAxis: 'y', plugins: {legend: {display: false}}}
});
