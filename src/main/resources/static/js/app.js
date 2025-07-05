document.addEventListener('DOMContentLoaded', () => {
    // --- Selección de Elementos del DOM ---
    const searchForm = document.getElementById('search-form');
    const productNameInput = document.getElementById('product-name-input');
    const searchButton = document.getElementById('search-button');
    const resultsArea = document.getElementById('results-area');
    const infoContainer = document.getElementById('info-container');
    const infoMessage = document.getElementById('info-message');
    const cardTemplate = document.getElementById('product-card-template');
    const resultsGrid = document.getElementById('results-grid');
    const loadingSpinner = document.getElementById('loading-spinner');
    const sortDropdown = document.getElementById('sort-dropdown');

    // --- Variables de Estado ---
    let currentProducts = [];
    let currentSort = 'price-asc';

    // --- Lógica de Búsqueda (VERSIÓN ACTUALIZADA) ---
    const startSearch = async (event) => {
        if (event) event.preventDefault();

        const productName = productNameInput.value.trim();
        console.log("Término de búsqueda:", productName); // Debug 1

        if (!productName) {
            showInfoMessage('Por favor, ingresa un nombre de producto.', 'warning');
            return;
        }

        prepareUIForSearch();

        try {
            const apiUrl = `/api/products/search?name=${encodeURIComponent(productName)}`;
            console.log("URL de la API:", apiUrl); // Debug 2

            const options = {
                method: 'GET',
                headers: {
                    'Accept': 'application/json'
                }
            };

            console.log("Enviando solicitud..."); // Debug 3
            const response = await fetch(apiUrl, options);
            console.log("Respuesta recibida. Status:", response.status); // Debug 4

            if (!response.ok) {
                const errorText = await response.text();
                console.error("Error en la respuesta:", errorText); // Debug 5
                throw new Error(`Error ${response.status}: ${errorText}`);
            }

            const products = await response.json();
            console.log("Datos recibidos:", products); // Debug 6
            currentProducts = products;
            renderResults(products);

        } catch (error) {
            console.error("Error completo:", error); // Debug 7
            showInfoMessage(
                `Error al buscar: ${error.message}`,
                'danger'
            );
        } finally {
            restoreUIAfterSearch();
        }
    };

    // --- Asignación de Eventos ---
    searchForm.addEventListener('submit', startSearch);

    // Eventos para ordenación
    document.querySelectorAll('.sort-option').forEach(option => {
        option.addEventListener('click', (e) => {
            e.preventDefault();
            sortProducts(e.target.dataset.sort);
        });
    });

    // --- Funciones de Manipulación de la UI ---

    function prepareUIForSearch() {
        infoContainer.hidden = true;
        resultsArea.hidden = true;
        loadingSpinner.hidden = false;
        searchButton.disabled = true;
        searchButton.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Buscando...';
    }

    function restoreUIAfterSearch() {
        loadingSpinner.hidden = true;
        resultsArea.hidden = false;
        searchButton.disabled = false;
        searchButton.innerHTML = '<i class="bi bi-search me-2"></i>Buscar';
    }

    function sortProducts(sortMethod) {
        currentSort = sortMethod;

        // Actualizar UI del dropdown
        document.querySelectorAll('.sort-option').forEach(option => {
            option.classList.toggle('active', option.dataset.sort === sortMethod);
        });

        // Ordenar productos
        const sortedProducts = [...currentProducts].sort((a, b) => {
            switch (sortMethod) {
                case 'price-asc':
                    return parsePrice(a.price) - parsePrice(b.price);
                case 'price-desc':
                    return parsePrice(b.price) - parsePrice(a.price);
                case 'rating':
                    return (b.rating || 0) - (a.rating || 0);
                case 'store':
                    return a.storeName.localeCompare(b.storeName);
                default:
                    return 0;
            }
        });

        renderResults(sortedProducts);
    }

    function parsePrice(priceStr) {
        return parseFloat(priceStr.replace(/[^\d.,]/g, '').replace(',', '.'));
    }

    function renderResults(products) {
        resultsGrid.innerHTML = '';

        if (products.length === 0) {
            showInfoMessage(
                'No encontramos productos con ese nombre. Intenta ser más específico o usar otras palabras clave.',
                'info'
            );
            return;
        }

        // Actualizar contador de resultados
        document.getElementById('results-count').textContent = `${products.length} resultados encontrados`;

        // Crear tarjetas para cada producto
        products.forEach(product => {
            resultsGrid.appendChild(createProductCard(product));
        });
    }

    function createProductCard(product) {
        const cardClone = cardTemplate.content.cloneNode(true);
        const card = cardClone.querySelector('.card');

        // Llenar datos básicos
        const img = card.querySelector('img');
        img.src = product.imageUrl || 'https://via.placeholder.com/300x200.png?text=Sin+Imagen';
        img.alt = product.name;

        card.querySelector('.card-title').textContent = product.name;
        card.querySelector('.store-name strong').textContent = product.storeName;
        card.querySelector('.card-price').textContent = formatPrice(product.price);
        card.querySelector('a').href = product.url;

        // Configurar disponibilidad
        const availabilityBadge = card.querySelector('.availability-badge');
        if (product.available) {
            availabilityBadge.className = 'badge availability-badge bg-success';
            availabilityBadge.textContent = 'Disponible';
        } else {
            availabilityBadge.className = 'badge availability-badge bg-danger';
            availabilityBadge.textContent = 'Agotado';
        }

        // Configurar rating si existe
        if (product.rating) {
            const starsContainer = card.querySelector('.rating-stars');
            starsContainer.innerHTML = generateRatingStars(product.rating);
        }

        // Configurar tiempo de actualización
        if (product.updatedAt) {
            card.querySelector('.time-ago').textContent = formatTimeAgo(product.updatedAt);
        }

        return cardClone;
    }

    function formatPrice(price) {
        // Formatear precio como moneda
        const number = parsePrice(price);
        return new Intl.NumberFormat('es-MX', {
            style: 'currency',
            currency: 'MXN'
        }).format(number);
    }

    function formatTimeAgo(dateString) {
        // Implementar lógica para mostrar "hace X tiempo"
        const date = new Date(dateString);
        const now = new Date();
        const diff = now - date;

        const minutes = Math.floor(diff / (1000 * 60));
        if (minutes < 60) return `hace ${minutes} min`;

        const hours = Math.floor(minutes / 60);
        if (hours < 24) return `hace ${hours} h`;

        const days = Math.floor(hours / 24);
        return `hace ${days} días`;
    }

    function generateRatingStars(rating) {
        // Generar estrellas basadas en el rating (0-5)
        const fullStars = Math.floor(rating);
        const hasHalfStar = rating % 1 >= 0.5;
        let starsHTML = '';

        for (let i = 0; i < 5; i++) {
            if (i < fullStars) {
                starsHTML += '<i class="bi bi-star-fill"></i>';
            } else if (i === fullStars && hasHalfStar) {
                starsHTML += '<i class="bi bi-star-half"></i>';
            } else {
                starsHTML += '<i class="bi bi-star"></i>';
            }
        }

        return starsHTML;
    }

    function showInfoMessage(message, type = 'info') {
        infoContainer.hidden = false;
        infoContainer.className = `alert alert-${type} alert-dismissible fade show`;
        infoMessage.textContent = message;
    }
});