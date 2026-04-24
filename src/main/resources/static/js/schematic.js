(function () {
    'use strict';

    var currentSort = null;

    function getTableItems() {
        return Array.from(document.querySelectorAll('#tables [data-table-name]'));
    }

    function applyFilter() {
        var filterInput = document.getElementById('table-filter');
        var term = filterInput ? filterInput.value.toLowerCase() : '';
        var items = getTableItems();
        var visibleCount = 0;

        items.forEach(function (el) {
            var matches = el.dataset.tableName.toLowerCase().indexOf(term) !== -1;
            el.style.display = matches ? '' : 'none';
            if (matches) visibleCount++;
        });

        var noResults = document.getElementById('schematic-no-results');
        if (noResults) {
            noResults.style.display = (term && visibleCount === 0) ? '' : 'none';
        }
    }

    function applySort(direction) {
        currentSort = direction;
        var items = getTableItems();
        if (items.length === 0) return;

        items.sort(function (a, b) {
            var nameA = a.dataset.tableName.toLowerCase();
            var nameB = b.dataset.tableName.toLowerCase();
            var cmp = nameA < nameB ? -1 : nameA > nameB ? 1 : 0;
            return direction === 'asc' ? cmp : -cmp;
        });

        var parent = items[0].parentNode;
        items.forEach(function (el) { parent.appendChild(el); });

        applyFilter();

        document.querySelectorAll('#sort-asc, #sort-desc').forEach(function (btn) {
            btn.classList.remove('active');
        });
        var activeBtn = document.getElementById(direction === 'asc' ? 'sort-asc' : 'sort-desc');
        if (activeBtn) activeBtn.classList.add('active');
    }

    function reapply() {
        if (currentSort) {
            applySort(currentSort);
        } else {
            applyFilter();
        }
    }

    function init() {
        var filterInput = document.getElementById('table-filter');
        var sortAsc = document.getElementById('sort-asc');
        var sortDesc = document.getElementById('sort-desc');

        if (filterInput) filterInput.addEventListener('input', applyFilter);
        if (sortAsc) sortAsc.addEventListener('click', function () { applySort('asc'); });
        if (sortDesc) sortDesc.addEventListener('click', function () { applySort('desc'); });

        document.addEventListener('htmx:afterSwap', function (e) {
            if (e.detail.target && e.detail.target.id === 'tables') {
                reapply();
            }
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
}());