package com.happyhome.transfer.favorite.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.happyhome.transfer.dao.TransferDao;
import com.happyhome.transfer.dto.TransferDto;
import com.happyhome.transfer.favorite.dao.FavoriteTransferDao;
<<<<<<< HEAD
=======
import com.happyhome.transfer.service.TransferImageStorage;
>>>>>>> 03d9a75b479f8cd98c05cc82cf66580557ccc14f
import com.happyhome.transfer.service.TransferService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FavoriteTransferServiceTest {

    @Test
    void toggleAddsAndRemovesFavoriteTransfer() {
        FakeFavoriteTransferDao favoriteDao = new FakeFavoriteTransferDao();
        FavoriteTransferService service = new FavoriteTransferService(
                favoriteDao,
<<<<<<< HEAD
                new TransferService(new FakeTransferDao())
=======
                new TransferService(new FakeTransferDao(), fakeImageStorage())
>>>>>>> 03d9a75b479f8cd98c05cc82cf66580557ccc14f
        );

        assertThat(service.toggle("ssafy", 7)).isTrue();
        assertThat(service.isFavorite("ssafy", 7)).isTrue();

        assertThat(service.toggle("ssafy", 7)).isFalse();
        assertThat(service.isFavorite("ssafy", 7)).isFalse();
    }

    @Test
    void findFavoritesReturnsExistingTransferDtos() {
        FakeFavoriteTransferDao favoriteDao = new FakeFavoriteTransferDao();
        favoriteDao.save("ssafy", 7);
        favoriteDao.save("ssafy", 8);
        FakeTransferDao transferDao = new FakeTransferDao();
        transferDao.transfers.put(7, transfer(7, "First"));

        FavoriteTransferService service = new FavoriteTransferService(
                favoriteDao,
<<<<<<< HEAD
                new TransferService(transferDao)
=======
                new TransferService(transferDao, fakeImageStorage())
>>>>>>> 03d9a75b479f8cd98c05cc82cf66580557ccc14f
        );

        List<TransferDto> favorites = service.findFavorites("ssafy", 100);

        assertThat(favorites).extracting(TransferDto::getTransferId).containsExactly(7);
    }

    private static TransferDto transfer(int transferId, String title) {
        TransferDto transfer = new TransferDto();
        transfer.setTransferId(transferId);
        transfer.setTitle(title);
        return transfer;
    }

<<<<<<< HEAD
=======
    private static TransferImageStorage fakeImageStorage() {
        return file -> "https://example.com/fake.jpg";
    }

>>>>>>> 03d9a75b479f8cd98c05cc82cf66580557ccc14f
    private static class FakeFavoriteTransferDao extends FavoriteTransferDao {

        private final Map<String, Set<Integer>> favorites = new HashMap<>();

        FakeFavoriteTransferDao() {
            super(null);
        }

        @Override
        public boolean exists(String userId, int transferId) {
            return favorites.getOrDefault(userId, Set.of()).contains(transferId);
        }

        @Override
        public void save(String userId, int transferId) {
            favorites.computeIfAbsent(userId, ignored -> new HashSet<>()).add(transferId);
        }

        @Override
        public void delete(String userId, int transferId) {
            favorites.getOrDefault(userId, Set.of()).remove(transferId);
        }

        @Override
        public List<Integer> findTransferIdsByUserId(String userId, int limit) {
            return new ArrayList<>(favorites.getOrDefault(userId, Set.of()));
        }
    }

    private static class FakeTransferDao extends TransferDao {

        private final Map<Integer, TransferDto> transfers = new HashMap<>();

        FakeTransferDao() {
            super(null);
        }

        @Override
        public Optional<TransferDto> findById(int transferId) {
            return Optional.ofNullable(transfers.get(transferId));
        }
    }
}
